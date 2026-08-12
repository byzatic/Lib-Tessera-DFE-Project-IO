package io.github.byzatic.lib.configio.infrastructure.dao;

import io.github.byzatic.lib.configio.application.dao.PipelineDaoInterface;
import io.github.byzatic.lib.configio.domain.exception.ProjectLoadingException;
import io.github.byzatic.lib.configio.domain.model.GraphNodeReferenceDataObject;
import io.github.byzatic.lib.configio.domain.model.NodeDataObject;
import io.github.byzatic.lib.configio.domain.model.PipelineDataObject;
import io.github.byzatic.lib.configio.domain.model.ProjectStructureDataObject;
import io.github.byzatic.lib.configio.infrastructure.dto.raw.node.pipeline.Pipeline;
import io.github.byzatic.lib.configio.infrastructure.strategy.NodePathResolverStrategy;
import io.github.byzatic.lib.configio.infrastructure.util.ConfigurationDataMapperUtility;
import io.github.byzatic.lib.configio.infrastructure.util.GsonJsonFileReaderUtility;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class GsonPipelineDao implements PipelineDaoInterface {

    private final GsonJsonFileReaderUtility jsonFileReader;
    private final NodePathResolverStrategy nodePathResolver;
    private final ConfigurationDataMapperUtility dataMapper;

    public GsonPipelineDao() {
        this(
                new GsonJsonFileReaderUtility(),
                new NodePathResolverStrategy(),
                new ConfigurationDataMapperUtility()
        );
    }

    public GsonPipelineDao(
            GsonJsonFileReaderUtility jsonFileReader,
            NodePathResolverStrategy nodePathResolver,
            ConfigurationDataMapperUtility dataMapper
    ) {
        this.jsonFileReader = Objects.requireNonNull(jsonFileReader, "jsonFileReader");
        this.nodePathResolver = Objects.requireNonNull(nodePathResolver, "nodePathResolver");
        this.dataMapper = Objects.requireNonNull(dataMapper, "dataMapper");
    }

    @Override
    public Map<GraphNodeReferenceDataObject, PipelineDataObject> load(
            Path projectDirectory,
            ProjectStructureDataObject projectStructure
    ) throws ProjectLoadingException {
        Map<GraphNodeReferenceDataObject, PipelineDataObject> result =
                new LinkedHashMap<GraphNodeReferenceDataObject, PipelineDataObject>();

        for (Map.Entry<GraphNodeReferenceDataObject, NodeDataObject> entry
                : projectStructure.getNodes().entrySet()) {
            Path nodeFile = nodePathResolver
                    .resolve(projectDirectory, entry.getValue())
                    .resolve("pipeline.json");
            Pipeline pipeline = jsonFileReader.read(nodeFile, Pipeline.class);
            result.put(entry.getKey(), dataMapper.mapPipeline(pipeline));
        }
        return result;
    }
}
