package io.github.byzatic.lib.configio.infrastructure.dao;

import io.github.byzatic.lib.configio.application.dao.NodeGlobalDaoInterface;
import io.github.byzatic.lib.configio.domain.exception.ProjectLoadingException;
import io.github.byzatic.lib.configio.domain.model.GraphNodeReferenceDataObject;
import io.github.byzatic.lib.configio.domain.model.NodeDataObject;
import io.github.byzatic.lib.configio.domain.model.NodeGlobalDataObject;
import io.github.byzatic.lib.configio.domain.model.ProjectStructureDataObject;
import io.github.byzatic.lib.configio.infrastructure.dto.raw.node.global.NodeGlobal;
import io.github.byzatic.lib.configio.infrastructure.strategy.NodePathResolverStrategy;
import io.github.byzatic.lib.configio.infrastructure.util.ConfigurationDataMapperUtility;
import io.github.byzatic.lib.configio.infrastructure.util.GsonJsonFileReaderUtility;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class GsonNodeGlobalDao implements NodeGlobalDaoInterface {

    private final GsonJsonFileReaderUtility jsonFileReader;
    private final NodePathResolverStrategy nodePathResolver;
    private final ConfigurationDataMapperUtility dataMapper;

    public GsonNodeGlobalDao() {
        this(
                new GsonJsonFileReaderUtility(),
                new NodePathResolverStrategy(),
                new ConfigurationDataMapperUtility()
        );
    }

    public GsonNodeGlobalDao(
            GsonJsonFileReaderUtility jsonFileReader,
            NodePathResolverStrategy nodePathResolver,
            ConfigurationDataMapperUtility dataMapper
    ) {
        this.jsonFileReader = Objects.requireNonNull(jsonFileReader, "jsonFileReader");
        this.nodePathResolver = Objects.requireNonNull(nodePathResolver, "nodePathResolver");
        this.dataMapper = Objects.requireNonNull(dataMapper, "dataMapper");
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
}
