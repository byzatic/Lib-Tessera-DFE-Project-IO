package io.github.byzatic.lib.configio.infrastructure.dao;

import io.github.byzatic.lib.configio.application.dao.NodeGlobalDaoInterface;
import io.github.byzatic.lib.configio.domain.exception.ProjectLoadingException;
import io.github.byzatic.lib.configio.domain.exception.ProjectSavingException;
import io.github.byzatic.lib.configio.domain.model.GraphNodeReferenceDataObject;
import io.github.byzatic.lib.configio.domain.model.NodeDataObject;
import io.github.byzatic.lib.configio.domain.model.NodeGlobalDataObject;
import io.github.byzatic.lib.configio.domain.model.ProjectStructureDataObject;
import io.github.byzatic.lib.configio.infrastructure.dto.raw.node.global.NodeGlobal;
import io.github.byzatic.lib.configio.infrastructure.strategy.NodePathResolverStrategy;
import io.github.byzatic.lib.configio.infrastructure.util.ConfigurationDataMapperUtility;
import io.github.byzatic.lib.configio.infrastructure.util.GsonJsonFileReaderUtility;
import io.github.byzatic.lib.configio.infrastructure.utils.GsonJsonFileWriterUtility;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class GsonNodeGlobalDao implements NodeGlobalDaoInterface {

    private final GsonJsonFileReaderUtility jsonFileReader;
    private final NodePathResolverStrategy nodePathResolver;
    private final ConfigurationDataMapperUtility dataMapper;
    private final GsonJsonFileWriterUtility jsonFileWriter;

    public GsonNodeGlobalDao() {
        this(
                new GsonJsonFileReaderUtility(),
                new NodePathResolverStrategy(),
                new ConfigurationDataMapperUtility(),
                new GsonJsonFileWriterUtility()
        );
    }

    public GsonNodeGlobalDao(
            GsonJsonFileReaderUtility jsonFileReader,
            NodePathResolverStrategy nodePathResolver,
            ConfigurationDataMapperUtility dataMapper
    ) {
        this(
                jsonFileReader,
                nodePathResolver,
                dataMapper,
                new GsonJsonFileWriterUtility()
        );
    }

    public GsonNodeGlobalDao(
            GsonJsonFileReaderUtility jsonFileReader,
            NodePathResolverStrategy nodePathResolver,
            ConfigurationDataMapperUtility dataMapper,
            GsonJsonFileWriterUtility jsonFileWriter
    ) {
        this.jsonFileReader = Objects.requireNonNull(jsonFileReader, "jsonFileReader");
        this.nodePathResolver = Objects.requireNonNull(nodePathResolver, "nodePathResolver");
        this.dataMapper = Objects.requireNonNull(dataMapper, "dataMapper");
        this.jsonFileWriter = Objects.requireNonNull(jsonFileWriter, "jsonFileWriter");
    }

    @Override
    public Map<GraphNodeReferenceDataObject, NodeGlobalDataObject> load(
            Path projectDirectory,
            ProjectStructureDataObject projectStructure
    ) throws ProjectLoadingException {
        Map<GraphNodeReferenceDataObject, NodeGlobalDataObject> result =
                new LinkedHashMap<GraphNodeReferenceDataObject, NodeGlobalDataObject>();

        for (Map.Entry<GraphNodeReferenceDataObject, NodeDataObject> entry
                : projectStructure.getNodes().entrySet()) {
            Path nodeFile = nodePathResolver
                    .resolve(projectDirectory, entry.getValue())
                    .resolve("global.json");
            NodeGlobal nodeGlobal = jsonFileReader.read(nodeFile, NodeGlobal.class);
            result.put(entry.getKey(), dataMapper.mapNodeGlobal(nodeGlobal));
        }
        return result;
    }

    @Override
    public void save(
            Path projectDirectory,
            ProjectStructureDataObject projectStructure,
            Map<GraphNodeReferenceDataObject, NodeGlobalDataObject> nodeGlobals
    ) throws ProjectSavingException {
        for (Map.Entry<GraphNodeReferenceDataObject, NodeDataObject> entry
                : projectStructure.getNodes().entrySet()) {
            NodeGlobalDataObject nodeGlobal = nodeGlobals.get(entry.getKey());
            if (nodeGlobal == null) {
                throw new ProjectSavingException(
                        "Node global configuration is missing: " + entry.getKey()
                );
            }
            try {
                Path nodeFile = nodePathResolver
                        .resolve(projectDirectory, entry.getValue())
                        .resolve("global.json");
                jsonFileWriter.write(nodeFile, dataMapper.mapNodeGlobal(nodeGlobal));
            } catch (ProjectLoadingException exception) {
                throw new ProjectSavingException(
                        "Cannot resolve node directory: " + entry.getKey(),
                        exception
                );
            }
        }
    }
}
