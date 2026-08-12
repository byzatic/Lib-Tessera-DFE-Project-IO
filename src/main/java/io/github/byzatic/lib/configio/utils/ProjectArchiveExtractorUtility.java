package io.github.byzatic.lib.configio.utils;

import io.github.byzatic.commons.TempDirectory;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Extracts a project ZIP archive into a managed temporary directory.
 */
public final class ProjectArchiveExtractorUtility {

    private static final String TEMP_DIRECTORY_PREFIX = "tessera-project-";
    private static final String MAC_OS_METADATA_DIRECTORY = "__MACOSX";

    private ProjectArchiveExtractorUtility() {
    }

    @Contract("_ -> new")
    public static @NotNull ExtractedProjectDataObject extract(Path archivePath) throws IOException {
        Path normalizedArchivePath = validateArchivePath(archivePath);
        TempDirectory tempDirectory = new TempDirectory(TEMP_DIRECTORY_PREFIX);

        try {
            Path temporaryDirectoryPath = tempDirectory.getPath().toAbsolutePath().normalize();
            extractArchive(normalizedArchivePath, temporaryDirectoryPath);
            Path projectDirectoryPath = resolveProjectDirectory(temporaryDirectoryPath);

            return new ExtractedProjectDataObject(
                    normalizedArchivePath,
                    projectDirectoryPath,
                    tempDirectory
            );
        } catch (IOException exception) {
            deleteAfterFailure(tempDirectory, exception);
            throw exception;
        } catch (RuntimeException exception) {
            deleteAfterFailure(tempDirectory, exception);
            throw exception;
        }
    }

    @Contract("null -> fail")
    private static @NotNull Path validateArchivePath(Path archivePath) throws IOException {
        if (archivePath == null) {
            throw new IllegalArgumentException("archivePath must not be null");
        }

        Path normalizedArchivePath = archivePath.toAbsolutePath().normalize();
        if (!Files.isRegularFile(normalizedArchivePath)) {
            throw new IOException("Project archive does not exist: " + normalizedArchivePath);
        }

        return normalizedArchivePath;
    }

    private static void extractArchive(Path archivePath, Path targetDirectoryPath) throws IOException {
        Set<Path> extractedEntryPaths = new HashSet<Path>();

        InputStream fileInputStream = Files.newInputStream(archivePath);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

        try (ZipInputStream zipInputStream = new ZipInputStream(bufferedInputStream)) {
            ZipEntry zipEntry = zipInputStream.getNextEntry();

            while (zipEntry != null) {
                Path targetPath = resolveEntryPath(targetDirectoryPath, zipEntry);
                ensureEntryIsUnique(targetPath, extractedEntryPaths);

                if (zipEntry.isDirectory()) {
                    Files.createDirectories(targetPath);
                } else {
                    extractFile(zipInputStream, targetPath);
                }

                zipInputStream.closeEntry();
                zipEntry = zipInputStream.getNextEntry();
            }
        }
    }

    private static @NotNull Path resolveEntryPath(@NotNull Path targetDirectoryPath, @NotNull ZipEntry zipEntry)
            throws IOException {
        Path targetPath = targetDirectoryPath.resolve(zipEntry.getName()).normalize();

        if (!targetPath.startsWith(targetDirectoryPath)) {
            throw new IOException("ZIP entry escapes the temporary directory: " + zipEntry.getName());
        }

        return targetPath;
    }

    private static void ensureEntryIsUnique(Path targetPath, @NotNull Set<Path> extractedEntryPaths)
            throws IOException {
        if (!extractedEntryPaths.add(targetPath)) {
            throw new IOException("Duplicate ZIP entry: " + targetPath.getFileName());
        }
    }

    private static void extractFile(ZipInputStream zipInputStream, @NotNull Path targetPath)
            throws IOException {
        Path parentDirectoryPath = targetPath.getParent();
        if (parentDirectoryPath != null) {
            Files.createDirectories(parentDirectoryPath);
        }

        try (OutputStream outputStream = Files.newOutputStream(
                targetPath,
                StandardOpenOption.CREATE_NEW,
                StandardOpenOption.WRITE
        )) {
            byte[] buffer = new byte[8192];
            int bytesRead = zipInputStream.read(buffer);

            while (bytesRead != -1) {
                outputStream.write(buffer, 0, bytesRead);
                bytesRead = zipInputStream.read(buffer);
            }
        }
    }

    private static Path resolveProjectDirectory(Path temporaryDirectoryPath) throws IOException {
        Path singleDirectoryPath = null;
        int projectEntryCount = 0;

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(temporaryDirectoryPath)) {
            for (Path entryPath : directoryStream) {
                if (isMetadataEntry(entryPath)) {
                    continue;
                }

                projectEntryCount++;
                singleDirectoryPath = entryPath;
            }
        }

        if (projectEntryCount == 0) {
            throw new IOException("Project archive is empty");
        }

        if (projectEntryCount == 1 && Files.isDirectory(singleDirectoryPath)) {
            return singleDirectoryPath;
        }

        return temporaryDirectoryPath;
    }

    private static boolean isMetadataEntry(@NotNull Path entryPath) {
        String fileName = entryPath.getFileName().toString();
        return MAC_OS_METADATA_DIRECTORY.equals(fileName) || ".DS_Store".equals(fileName);
    }

    private static void deleteAfterFailure(TempDirectory tempDirectory, Throwable failure) {
        try {
            tempDirectory.delete();
        } catch (RuntimeException cleanupException) {
            failure.addSuppressed(cleanupException);
        }
    }
}
