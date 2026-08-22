package io.github.byzatic.tessera.lib.configio.application.saver;

import io.github.byzatic.tessera.lib.configio.domain.exception.ProjectSavingException;
import io.github.byzatic.tessera.lib.configio.domain.model.ProjectExportDataObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;

public final class ProjectV1ExporterStrategy implements ProjectExporterInterface {

    private final ProjectSaverInterface saver;

    public ProjectV1ExporterStrategy(ProjectSaverInterface saver) {
        this.saver = Objects.requireNonNull(saver, "saver");
    }

    @Override
    public Path export(ProjectExportDataObject project) throws ProjectSavingException {
        Objects.requireNonNull(project, "project");
        Path destination = withZipExtension(project.getArchiveDestination().toAbsolutePath().normalize());
        validateDestination(destination);
        Path temporaryDirectory = null;
        try {
            temporaryDirectory = Files.createTempDirectory("tessera-dfe-export-");
            Path projectDirectory = temporaryDirectory.resolve(archiveBaseName(destination));
            Path archive = saver.save(
                    projectDirectory,
                    project.getGlobal(),
                    project.getNodeContainer(),
                    project.getModuleJars(),
                    project.getServiceJars(),
                    project.getDslFiles()
            );
            Files.move(archive, destination, StandardCopyOption.REPLACE_EXISTING);
            return destination;
        } catch (ProjectSavingException exception) {
            throw exception;
        } catch (IOException exception) {
            throw new ProjectSavingException("Cannot export project archive: " + destination, exception);
        } finally {
            deleteTree(temporaryDirectory);
        }
    }

    private Path withZipExtension(Path destination) {
        String fileName = destination.getFileName().toString();
        return fileName.toLowerCase(Locale.ROOT).endsWith(".zip")
                ? destination
                : destination.resolveSibling(fileName + ".zip");
    }

    private String archiveBaseName(Path destination) {
        String fileName = destination.getFileName().toString();
        return fileName.substring(0, fileName.length() - ".zip".length());
    }

    private void validateDestination(Path destination) throws ProjectSavingException {
        if (Files.isDirectory(destination)) {
            throw new ProjectSavingException("Archive destination is a directory: " + destination);
        }
        Path parent = destination.getParent();
        if (parent == null || !Files.isDirectory(parent)) {
            throw new ProjectSavingException("Archive parent directory does not exist: " + parent);
        }
        if (archiveBaseName(destination).isBlank()) {
            throw new ProjectSavingException("Archive file name must not be blank");
        }
    }

    private void deleteTree(Path root) {
        if (root == null) return;
        try (var paths = Files.walk(root)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        } catch (IOException ignored) {
            // Cleanup failure does not invalidate an archive that has already been exported.
        }
    }
}
