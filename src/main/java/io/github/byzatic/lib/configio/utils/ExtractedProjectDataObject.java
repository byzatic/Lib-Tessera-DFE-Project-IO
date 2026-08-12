package io.github.byzatic.lib.configio.utils;

import io.github.byzatic.commons.TempDirectory;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Owns the temporary directory containing an extracted project.
 */
public final class ExtractedProjectDataObject implements AutoCloseable {

    private final Path originalArchivePath;
    private final Path temporaryDirectoryPath;
    private final Path extractedProjectDirectoryPath;
    private final TempDirectory tempDirectory;
    private boolean closed;

    ExtractedProjectDataObject(
            Path originalArchivePath,
            Path extractedProjectDirectoryPath,
            TempDirectory tempDirectory
    ) {
        this.originalArchivePath = Objects.requireNonNull(
                originalArchivePath,
                "originalArchivePath"
        );
        this.extractedProjectDirectoryPath = Objects.requireNonNull(
                extractedProjectDirectoryPath,
                "extractedProjectDirectoryPath"
        );
        this.tempDirectory = Objects.requireNonNull(tempDirectory, "tempDirectory");
        this.temporaryDirectoryPath = tempDirectory.getPath();
    }

    public Path getOriginalArchivePath() {
        return originalArchivePath;
    }

    public Path getTemporaryDirectoryPath() {
        return temporaryDirectoryPath;
    }

    public Path getExtractedProjectDirectoryPath() {
        return extractedProjectDirectoryPath;
    }

    public boolean isClosed() {
        return closed;
    }

    public void delete() {
        close();
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }

        tempDirectory.delete();
        closed = true;
    }
}
