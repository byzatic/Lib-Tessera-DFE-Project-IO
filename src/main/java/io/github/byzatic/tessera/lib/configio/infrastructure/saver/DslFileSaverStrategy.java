package io.github.byzatic.tessera.lib.configio.infrastructure.saver;

import io.github.byzatic.tessera.lib.configio.application.saver.DslFileSaverInterface;
import io.github.byzatic.tessera.lib.configio.domain.exception.ProjectLoadingException;
import io.github.byzatic.tessera.lib.configio.domain.exception.ProjectSavingException;
import io.github.byzatic.tessera.lib.configio.domain.model.DslFileDataObject;
import io.github.byzatic.tessera.lib.configio.domain.model.NodeContainerDataObject;
import io.github.byzatic.tessera.lib.configio.domain.model.NodeDataObject;
import io.github.byzatic.tessera.lib.configio.infrastructure.strategy.NodePathResolverStrategy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class DslFileSaverStrategy implements DslFileSaverInterface {

    private final NodePathResolverStrategy nodePathResolver;

    public DslFileSaverStrategy() {
        this(new NodePathResolverStrategy());
    }

    public DslFileSaverStrategy(NodePathResolverStrategy nodePathResolver) {
        this.nodePathResolver = Objects.requireNonNull(nodePathResolver, "nodePathResolver");
    }

    @Override
    public void save(
            Path projectDirectory,
            NodeContainerDataObject nodeContainer,
            List<DslFileDataObject> dslFiles
    ) throws ProjectSavingException {
        Objects.requireNonNull(projectDirectory, "projectDirectory");
        Objects.requireNonNull(nodeContainer, "nodeContainer");
        Objects.requireNonNull(dslFiles, "dslFiles");
        Set<Path> targets = new HashSet<Path>();
        try {
            for (DslFileDataObject dslFile : dslFiles) {
                NodeDataObject node = nodeContainer.getProjectStructure().getNode(dslFile.getNodeReference());
                Path directory = nodePathResolver.resolve(projectDirectory, node)
                        .resolve("configuration_files");
                Path target = directory.resolve(dslFile.getFileName()).normalize();
                if (!target.startsWith(directory) || !targets.add(target)) {
                    throw new ProjectSavingException("Duplicate or invalid DSL file: " + target);
                }
                Files.createDirectories(directory);
                Files.writeString(target, dslFile.getContent(), StandardCharsets.UTF_8);
            }
        } catch (ProjectSavingException exception) {
            throw exception;
        } catch (ProjectLoadingException | IOException | IllegalArgumentException exception) {
            throw new ProjectSavingException("Cannot save DSL files", exception);
        }
    }
}
