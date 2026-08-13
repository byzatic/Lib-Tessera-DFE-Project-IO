package io.github.byzatic.lib.configio.infrastructure.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import io.github.byzatic.lib.configio.domain.exception.ProjectSavingException;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

public final class GsonJsonFileWriterUtility {

    private final Gson gson;

    public GsonJsonFileWriterUtility() {
        this(new GsonBuilder().setPrettyPrinting().create());
    }

    public GsonJsonFileWriterUtility(Gson gson) {
        this.gson = Objects.requireNonNull(gson, "gson");
    }

    public void write(Path filePath, Object value) throws ProjectSavingException {
        if (filePath == null) {
            throw new ProjectSavingException("Configuration file path must not be null");
        }
        if (value == null) {
            throw new ProjectSavingException("Configuration value must not be null");
        }

        Path normalizedFilePath = filePath.toAbsolutePath().normalize();
        Path parentDirectory = normalizedFilePath.getParent();
        if (parentDirectory == null) {
            throw new ProjectSavingException(
                    "Configuration file must have a parent directory: " + normalizedFilePath
            );
        }

        Path temporaryFile = null;
        try {
            Files.createDirectories(parentDirectory);
            temporaryFile = Files.createTempFile(
                    parentDirectory,
                    normalizedFilePath.getFileName().toString(),
                    ".tmp"
            );
            writeJson(temporaryFile, value);
            replaceFile(temporaryFile, normalizedFilePath);
        } catch (JsonIOException exception) {
            deleteTemporaryFile(temporaryFile, exception);
            throw new ProjectSavingException(
                    "Cannot serialize configuration file: " + normalizedFilePath,
                    exception
            );
        } catch (IOException exception) {
            deleteTemporaryFile(temporaryFile, exception);
            throw new ProjectSavingException(
                    "Cannot write configuration file: " + normalizedFilePath,
                    exception
            );
        }
    }

    private void writeJson(Path temporaryFile, Object value) throws IOException {
        try (Writer writer = Files.newBufferedWriter(
                temporaryFile,
                StandardCharsets.UTF_8
        )) {
            gson.toJson(value, writer);
        }
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
