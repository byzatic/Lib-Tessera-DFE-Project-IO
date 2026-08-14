package io.github.byzatic.lib.configio.infrastructure.revision;

import io.github.byzatic.lib.configio.application.loader.ProjectLoaderInterface;
import io.github.byzatic.lib.configio.application.revision.ProjectRevision;
import io.github.byzatic.lib.configio.application.revision.ProjectRevisionFailure;
import io.github.byzatic.lib.configio.application.revision.ProjectRevisionListener;
import io.github.byzatic.lib.configio.application.revision.ProjectRevisionSource;
import io.github.byzatic.lib.configio.domain.exception.ProjectLoadingException;
import io.github.byzatic.lib.configio.domain.exception.ProjectRevisionException;
import io.github.byzatic.lib.configio.domain.model.ProjectLoadResultDataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Polls one ZIP archive, waits until its file signature is stable, and publishes loaded
 * project revisions from isolated temporary directories.
 *
 * <p>Callbacks are serialized on the source executor. A listener must return quickly and
 * transfer longer work to its own executor.</p>
 */
public final class PollingZipProjectRevisionSource implements ProjectRevisionSource {

    private static final Logger logger =
            LoggerFactory.getLogger(PollingZipProjectRevisionSource.class);
    private static final int COPY_BUFFER_SIZE = 8192;

    private final ZipProjectRevisionSourceConfiguration configuration;
    private final ProjectLoaderInterface projectLoader;
    private final ScheduledExecutorService executor;
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final AtomicBoolean closed = new AtomicBoolean(false);

    private ProjectRevisionListener listener;
    private ArchiveSignature observedSignature;
    private int stableObservations;
    private String processedRevisionId;

    public PollingZipProjectRevisionSource(
            ZipProjectRevisionSourceConfiguration configuration,
            ProjectLoaderInterface projectLoader
    ) {
        this.configuration = Objects.requireNonNull(configuration, "configuration");
        this.projectLoader = Objects.requireNonNull(projectLoader, "projectLoader");
        this.executor = Executors.newSingleThreadScheduledExecutor(new RevisionThreadFactory());
    }

    @Override
    public void start(ProjectRevisionListener value) throws ProjectRevisionException {
        Objects.requireNonNull(value, "listener");
        if (closed.get()) {
            throw new IllegalStateException("Project revision source is closed");
        }
        if (!started.compareAndSet(false, true)) {
            throw new IllegalStateException("Project revision source is already started");
        }

        try {
            Files.createDirectories(configuration.getStagingDirectory());
        } catch (IOException exception) {
            started.set(false);
            throw new ProjectRevisionException(
                    "Cannot create project staging directory: "
                            + configuration.getStagingDirectory(),
                    exception
            );
        }

        listener = value;
        long intervalMillis = configuration.getPollInterval().toMillis();
        executor.scheduleWithFixedDelay(
                new PollTask(),
                0L,
                intervalMillis,
                TimeUnit.MILLISECONDS
        );
    }

    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        executor.shutdownNow();
        try {
            if (!executor.awaitTermination(10L, TimeUnit.SECONDS)) {
                logger.warn("Project revision source executor did not terminate in time");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    private void poll() {
        Path sourceArchive = configuration.getSourceArchive();
        if (!Files.isRegularFile(sourceArchive)) {
            resetObservation();
            return;
        }

        try {
            ArchiveSignature currentSignature = ArchiveSignature.read(sourceArchive);
            if (!currentSignature.equals(observedSignature)) {
                observedSignature = currentSignature;
                stableObservations = 1;
            } else {
                stableObservations++;
            }
            if (stableObservations < configuration.getStableObservationCount()) {
                return;
            }

            String revisionId = calculateSha256(sourceArchive);
            if (revisionId.equals(processedRevisionId)) {
                return;
            }
            processedRevisionId = revisionId;
            prepareAndPublish(sourceArchive, revisionId);
        } catch (Exception exception) {
            publishFailure(sourceArchive, null, exception);
        }
    }

    private void resetObservation() {
        observedSignature = null;
        stableObservations = 0;
    }

    private void prepareAndPublish(Path sourceArchive, String revisionId) {
        Path revisionDirectory = null;
        ProjectLoadResultDataObject loadedProject = null;
        try {
            revisionDirectory = Files.createTempDirectory(
                    configuration.getStagingDirectory(),
                    "project-" + revisionId.substring(0, 12) + "-"
            );
            Path stagedArchive = revisionDirectory.resolve("project.zip");
            Files.copy(sourceArchive, stagedArchive, StandardCopyOption.REPLACE_EXISTING);

            String stagedRevisionId = calculateSha256(stagedArchive);
            if (!revisionId.equals(stagedRevisionId)) {
                throw new ProjectRevisionException(
                        "Project archive changed while it was being staged: " + sourceArchive
                );
            }

            Path extractionDirectory = revisionDirectory.resolve("extracted");
            Files.createDirectory(extractionDirectory);
            extract(stagedArchive, extractionDirectory);
            Path projectDirectory = locateProjectDirectory(extractionDirectory);
            loadedProject = projectLoader.load(projectDirectory);

            ProjectRevision revision = new ProjectRevision(
                    revisionId,
                    sourceArchive.toAbsolutePath().normalize(),
                    projectDirectory,
                    revisionDirectory,
                    loadedProject
            );
            loadedProject = null;
            revisionDirectory = null;
            publishRevision(revision);
        } catch (Exception exception) {
            closeAfterFailure(loadedProject, revisionDirectory, exception);
            publishFailure(sourceArchive, revisionId, exception);
        }
    }

    private void extract(Path archive, Path targetDirectory)
            throws IOException, ProjectRevisionException {
        int entryCount = 0;
        long expandedBytes = 0L;
        byte[] buffer = new byte[COPY_BUFFER_SIZE];

        try (InputStream fileInput = Files.newInputStream(archive);
             ZipInputStream zipInput = new ZipInputStream(new BufferedInputStream(fileInput))) {
            ZipEntry entry;
            while ((entry = zipInput.getNextEntry()) != null) {
                entryCount++;
                if (entryCount > configuration.getMaximumEntryCount()) {
                    throw new ProjectRevisionException("Project archive contains too many entries");
                }

                Path destination = resolveEntry(targetDirectory, entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(destination);
                } else {
                    Path parent = destination.getParent();
                    if (parent != null) {
                        Files.createDirectories(parent);
                    }
                    try (OutputStream output = new BufferedOutputStream(
                            Files.newOutputStream(destination))) {
                        int read;
                        while ((read = zipInput.read(buffer)) != -1) {
                            expandedBytes += read;
                            if (expandedBytes > configuration.getMaximumExpandedBytes()) {
                                throw new ProjectRevisionException(
                                        "Expanded project archive exceeds configured size limit"
                                );
                            }
                            output.write(buffer, 0, read);
                        }
                    }
                }
                zipInput.closeEntry();
            }
        }
    }

    private Path resolveEntry(Path targetDirectory, String entryName)
            throws ProjectRevisionException {
        if (entryName == null || entryName.trim().isEmpty()) {
            throw new ProjectRevisionException("Project archive contains an empty entry name");
        }
        Path destination = targetDirectory.resolve(entryName).normalize();
        if (!destination.startsWith(targetDirectory)) {
            throw new ProjectRevisionException(
                    "Project archive entry escapes the staging directory: " + entryName
            );
        }
        return destination;
    }

    private Path locateProjectDirectory(Path extractionDirectory)
            throws IOException, ProjectRevisionException {
        final List<Path> projectDirectories = new ArrayList<Path>();
        Files.walkFileTree(extractionDirectory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
                if (attributes.isRegularFile()
                        && "Project.json".equals(file.getFileName().toString())) {
                    Path dataDirectory = file.getParent();
                    if (dataDirectory != null
                            && "data".equals(dataDirectory.getFileName().toString())) {
                        projectDirectories.add(dataDirectory.getParent());
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });

        if (projectDirectories.size() == 1) {
            return projectDirectories.get(0);
        }
        throw new ProjectRevisionException(
                "Archive must contain exactly one project with data/Project.json"
        );
    }

    private String calculateSha256(Path file)
            throws IOException, ProjectRevisionException {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException exception) {
            throw new ProjectRevisionException("SHA-256 is unavailable", exception);
        }

        byte[] buffer = new byte[COPY_BUFFER_SIZE];
        try (InputStream input = new DigestInputStream(Files.newInputStream(file), digest)) {
            while (input.read(buffer) != -1) {
                // DigestInputStream updates the digest while reading.
            }
        }

        byte[] hash = digest.digest();
        StringBuilder result = new StringBuilder(hash.length * 2);
        for (byte value : hash) {
            result.append(String.format("%02x", value & 0xff));
        }
        return result.toString();
    }

    private void publishRevision(ProjectRevision revision) {
        try {
            listener.onRevisionAvailable(revision);
        } catch (RuntimeException exception) {
            try {
                revision.close();
            } catch (IOException closeException) {
                exception.addSuppressed(closeException);
            }
            publishFailure(configuration.getSourceArchive(), revision.getRevisionId(), exception);
        }
    }

    private void publishFailure(Path sourceArchive, String revisionId, Throwable cause) {
        logger.error("Project revision {} was rejected", revisionId, cause);
        try {
            listener.onRevisionRejected(
                    new ProjectRevisionFailure(sourceArchive, revisionId, cause)
            );
        } catch (RuntimeException listenerFailure) {
            logger.error("Project revision failure listener failed", listenerFailure);
        }
    }

    private void closeAfterFailure(
            ProjectLoadResultDataObject loadedProject,
            Path revisionDirectory,
            Throwable failure
    ) {
        if (loadedProject != null) {
            try {
                loadedProject.close();
            } catch (IOException exception) {
                failure.addSuppressed(exception);
            }
        }
        if (revisionDirectory != null) {
            try {
                deleteRecursively(revisionDirectory);
            } catch (IOException exception) {
                failure.addSuppressed(exception);
            }
        }
    }

    private void deleteRecursively(Path directory) throws IOException {
        if (!Files.exists(directory)) {
            return;
        }
        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attributes)
                    throws IOException {
                Files.deleteIfExists(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path visitedDirectory, IOException failure)
                    throws IOException {
                if (failure != null) {
                    throw failure;
                }
                Files.deleteIfExists(visitedDirectory);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private final class PollTask implements Runnable {
        @Override
        public void run() {
            if (!closed.get()) {
                poll();
            }
        }
    }

    private static final class RevisionThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "project-revision-source");
            thread.setDaemon(true);
            return thread;
        }
    }

    private static final class ArchiveSignature {

        private final long size;
        private final long modifiedMillis;

        private ArchiveSignature(long size, long modifiedMillis) {
            this.size = size;
            this.modifiedMillis = modifiedMillis;
        }

        private static ArchiveSignature read(Path archive) throws IOException {
            return new ArchiveSignature(
                    Files.size(archive),
                    Files.getLastModifiedTime(archive).toMillis()
            );
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof ArchiveSignature)) {
                return false;
            }
            ArchiveSignature that = (ArchiveSignature) other;
            return size == that.size && modifiedMillis == that.modifiedMillis;
        }

        @Override
        public int hashCode() {
            int result = Long.hashCode(size);
            result = 31 * result + Long.hashCode(modifiedMillis);
            return result;
        }
    }
}
