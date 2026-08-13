package io.github.byzatic.lib.configio.infrastructure.utils;

import io.github.byzatic.lib.configio.domain.exception.ProjectSavingException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ProjectDirectoryInitializerUtility {

    public void initialize(Path projectDirectory) throws ProjectSavingException {
        try {
            Files.createDirectories(projectDirectory.resolve("data").resolve("nodes"));
            Files.createDirectories(projectDirectory.resolve("modules").resolve("shared"));
        } catch (IOException exception) {
            throw new ProjectSavingException(
                    "Cannot initialize project directory: " + projectDirectory,
                    exception
            );
        }
    }
}
