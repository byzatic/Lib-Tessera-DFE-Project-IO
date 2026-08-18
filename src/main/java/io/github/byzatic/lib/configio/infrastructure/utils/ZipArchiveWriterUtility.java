package io.github.byzatic.lib.configio.infrastructure.utils;

import io.github.byzatic.lib.configio.domain.exception.ProjectSavingException;

import java.io.File;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class ZipArchiveWriterUtility {

    public Path archive(Path projectDirectory) throws ProjectSavingException {
        Path normalizedProjectDirectory = validateProjectDirectory(projectDirectory);
        Path archive = resolveArchive(normalizedProjectDirectory);
        Path temporaryArchive = null;
        try {
            temporaryArchive = Files.createTempFile(
                    archive.getParent(),
                    archive.getFileName().toString(),
                    ".tmp"
            );
            writeArchive(normalizedProjectDirectory, temporaryArchive);
            replaceFile(temporaryArchive, archive);
            return archive;
        } catch (IOException exception) {
            deleteTemporaryFile(temporaryArchive, exception);
            throw new ProjectSavingException(
                    "Cannot create project archive: " + archive,
                    exception
            );
        }
    }

    private Path validateProjectDirectory(Path projectDirectory)
            throws ProjectSavingException {
        if (projectDirectory == null) {
            throw new ProjectSavingException("Project directory must not be null");
        }
        Path normalizedProjectDirectory = projectDirectory.toAbsolutePath().normalize();
        if (!Files.isDirectory(normalizedProjectDirectory)) {
            throw new ProjectSavingException(
                    "Project directory does not exist: " + normalizedProjectDirectory
            );
        }
        if (normalizedProjectDirectory.getFileName() == null
                || normalizedProjectDirectory.getParent() == null) {
            throw new ProjectSavingException(
                    "Project directory must have a name and parent: "
                            + normalizedProjectDirectory
            );
        }
        return normalizedProjectDirectory;
    }

    private Path resolveArchive(Path projectDirectory) {
        String archiveName = projectDirectory.getFileName().toString() + ".zip";
        return projectDirectory.getParent().resolve(archiveName);
    }

    private void writeArchive(Path projectDirectory, Path archive) throws IOException {
        List<Path> entries = collectEntries(projectDirectory);
        try (ZipOutputStream output = new ZipOutputStream(Files.newOutputStream(archive))) {
            for (Path entry : entries) {
                writeEntry(projectDirectory, entry, output);
            }
        }
    }

    private List<Path> collectEntries(final Path projectDirectory) throws IOException {
        final List<Path> entries = new ArrayList<Path>();
        Files.walkFileTree(projectDirectory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(
                    Path directory,
                    BasicFileAttributes attributes
            ) {
                entries.add(directory);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
                entries.add(file);
                return FileVisitResult.CONTINUE;
            }
        });
        Collections.sort(entries, new Comparator<Path>() {
            @Override
            public int compare(Path first, Path second) {
                return first.toString().compareTo(second.toString());
            }
        });
        return entries;
    }

    private void writeEntry(
            Path projectDirectory,
            Path source,
            ZipOutputStream output
    ) throws IOException {
        String entryName = resolveEntryName(projectDirectory, source);
        boolean directory = Files.isDirectory(source);
        if (directory) {
            entryName = entryName + "/";
        }

        output.putNextEntry(new ZipEntry(entryName));
        if (!directory) {
            Files.copy(source, output);
        }
        output.closeEntry();
    }

    private String resolveEntryName(Path projectDirectory, Path source) {
        Path archiveRoot = projectDirectory.getFileName();
        Path entryPath = archiveRoot;
        if (!projectDirectory.equals(source)) {
            entryPath = archiveRoot.resolve(projectDirectory.relativize(source));
        }
        return entryPath.toString().replace(File.separatorChar, '/');
    }

    private void replaceFile(Path source, Path target) throws IOException {
        try {
            Files.move(
                    source,
                    target,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING
            );
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void deleteTemporaryFile(Path temporaryFile, Throwable failure) {
        if (temporaryFile == null) {
            return;
        }
        try {
            Files.deleteIfExists(temporaryFile);
        } catch (IOException exception) {
            failure.addSuppressed(exception);
        }
    }
}
