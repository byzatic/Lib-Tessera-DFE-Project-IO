package io.github.byzatic.tessera.lib.configio.infrastructure.utils;

import io.github.byzatic.tessera.lib.configio.domain.exception.PluginSavingException;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class JarPluginSaverUtility {

    public Path save(
            Path sourceJar,
            Path projectDirectory,
            Path pluginDirectory
    ) throws PluginSavingException {
        Path normalizedSource = validateSourceJar(sourceJar);
        Path normalizedProjectDirectory = normalizeProjectDirectory(projectDirectory);
        if (pluginDirectory == null || pluginDirectory.isAbsolute()) {
            throw new PluginSavingException(
                    "Plugin directory must be a relative path"
            );
        }

        Path targetDirectory = normalizedProjectDirectory
                .resolve(pluginDirectory)
                .normalize();
        if (!targetDirectory.startsWith(normalizedProjectDirectory)) {
            throw new PluginSavingException(
                    "Plugin directory escapes project directory: " + pluginDirectory
            );
        }

        Path targetJar = targetDirectory
                .resolve(normalizedSource.getFileName())
                .normalize();
        if (normalizedSource.equals(targetJar)) {
            return targetJar;
        }

        Path temporaryJar = null;
        try {
            Files.createDirectories(targetDirectory);
            temporaryJar = Files.createTempFile(
                    targetDirectory,
                    normalizedSource.getFileName().toString(),
                    ".tmp"
            );
            Files.copy(
                    normalizedSource,
                    temporaryJar,
                    StandardCopyOption.REPLACE_EXISTING
            );
            replaceFile(temporaryJar, targetJar);
            return targetJar;
        } catch (IOException exception) {
            deleteTemporaryFile(temporaryJar, exception);
            throw new PluginSavingException(
                    "Cannot save plugin JAR to project: " + targetJar,
                    exception
            );
        }
    }

    private Path validateSourceJar(Path sourceJar) throws PluginSavingException {
        if (sourceJar == null) {
            throw new PluginSavingException("Source JAR must not be null");
        }
        Path normalizedSource = sourceJar.toAbsolutePath().normalize();
        if (!Files.isRegularFile(normalizedSource)) {
            throw new PluginSavingException(
                    "Source JAR does not exist: " + normalizedSource
            );
        }
        if (!normalizedSource.getFileName().toString().endsWith(".jar")) {
            throw new PluginSavingException(
                    "Plugin file must have a .jar extension: " + normalizedSource
            );
        }
        return normalizedSource;
    }

    private Path normalizeProjectDirectory(Path projectDirectory)
            throws PluginSavingException {
        if (projectDirectory == null) {
            throw new PluginSavingException("Project directory must not be null");
        }
        return projectDirectory.toAbsolutePath().normalize();
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
