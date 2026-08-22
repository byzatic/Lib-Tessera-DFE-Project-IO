package io.github.byzatic.lib.configio.unified.internal;

import io.github.byzatic.lib.configio.domain.model.ConfigurationFileDataObject;
import io.github.byzatic.lib.configio.domain.model.ConfigurationOptionDataObject;
import io.github.byzatic.lib.configio.domain.model.DslFileDataObject;
import io.github.byzatic.lib.configio.domain.model.GraphNodeReferenceDataObject;
import io.github.byzatic.lib.configio.domain.model.NodeContainerDataObject;
import io.github.byzatic.lib.configio.domain.model.NodeDataObject;
import io.github.byzatic.lib.configio.domain.model.NodeGlobalDataObject;
import io.github.byzatic.lib.configio.domain.model.PipelineDataObject;
import io.github.byzatic.lib.configio.domain.model.ProjectDataObject;
import io.github.byzatic.lib.configio.domain.model.ProjectGlobalDataObject;
import io.github.byzatic.lib.configio.domain.model.ProjectLoadResultDataObject;
import io.github.byzatic.lib.configio.domain.model.ProjectStructureDataObject;
import io.github.byzatic.lib.configio.domain.model.RoutineEditorMetadataDataObject;
import io.github.byzatic.lib.configio.domain.model.ServiceEditorMetadataDataObject;
import io.github.byzatic.lib.configio.domain.model.ServiceDataObject;
import io.github.byzatic.lib.configio.domain.model.StageConsistencyDataObject;
import io.github.byzatic.lib.configio.domain.model.StageDescriptionDataObject;
import io.github.byzatic.lib.configio.domain.model.StorageDataObject;
import io.github.byzatic.lib.configio.domain.model.WorkerDescriptionDataObject;
import io.github.byzatic.lib.configio.routine_spi.RoutineFunctionDescriptor;
import io.github.byzatic.lib.configio.unified.model.ConfigurationFile;
import io.github.byzatic.lib.configio.unified.model.DslSource;
import io.github.byzatic.lib.configio.unified.model.NodeConfiguration;
import io.github.byzatic.lib.configio.unified.model.NodeId;
import io.github.byzatic.lib.configio.unified.model.Pipeline;
import io.github.byzatic.lib.configio.unified.model.PipelineStage;
import io.github.byzatic.lib.configio.unified.model.ProjectConfiguration;
import io.github.byzatic.lib.configio.unified.model.ProjectNode;
import io.github.byzatic.lib.configio.unified.model.RoutineFunction;
import io.github.byzatic.lib.configio.unified.model.RoutineMetadata;
import io.github.byzatic.lib.configio.unified.model.ServiceDefinition;
import io.github.byzatic.lib.configio.unified.model.ServiceMetadata;
import io.github.byzatic.lib.configio.unified.model.ServiceOption;
import io.github.byzatic.lib.configio.unified.model.ServiceParameterMetadata;
import io.github.byzatic.lib.configio.unified.model.ServiceParameterType;
import io.github.byzatic.lib.configio.unified.model.ServiceStorageRole;
import io.github.byzatic.lib.configio.unified.model.StorageDefinition;
import io.github.byzatic.lib.configio.unified.model.StorageOption;
import io.github.byzatic.lib.configio.unified.model.TesseraProject;
import io.github.byzatic.lib.configio.unified.model.Worker;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

final class LegacyProjectMapper {

    TesseraProject toUnified(ProjectLoadResultDataObject source) {
        Objects.requireNonNull(source, "source");
        NodeContainerDataObject sourceNodes = source.getNodeContainer();
        Map<GraphNodeReferenceDataObject, NodeId> nodeIds =
                new LinkedHashMap<GraphNodeReferenceDataObject, NodeId>();
        for (GraphNodeReferenceDataObject reference
                : sourceNodes.getProjectStructure().getNodeReferences()) {
            NodeDataObject node = sourceNodes.getProjectStructure().getNode(reference);
            NodeId previous = nodeIds.put(reference, stableNodeId(node));
            if (previous != null) {
                throw new IllegalArgumentException("Duplicate project node reference: " + reference);
            }
        }
        if (nodeIds.values().stream().distinct().count() != nodeIds.size()) {
            throw new IllegalArgumentException("Project node id and name pairs must be unique");
        }

        Map<NodeId, ProjectNode> nodes = new LinkedHashMap<NodeId, ProjectNode>();
        for (GraphNodeReferenceDataObject reference
                : sourceNodes.getProjectStructure().getNodeReferences()) {
            NodeId nodeId = nodeIds.get(reference);
            NodeDataObject node = sourceNodes.getProjectStructure().getNode(reference);
            nodes.put(nodeId, ProjectNode.newBuilder()
                    .nodeId(nodeId)
                    .id(node.getId())
                    .name(node.getName())
                    .description(text(node.getDescription()))
                    .downstream(node.getDownstream().stream().map(nodeIds::get).toList())
                    .configuration(mapNodeConfiguration(sourceNodes.getNodeGlobal(reference)))
                    .pipeline(mapPipeline(sourceNodes.getPipeline(reference)))
                    .build());
        }
        ProjectDataObject project = sourceNodes.getProjectStructure().getProject();
        return TesseraProject.newBuilder()
                .formatVersion(project.getProjectConfigVersion())
                .name(project.getProjectName())
                .configuration(mapProjectConfiguration(source.getGlobal()))
                .nodes(nodes)
                .build();
    }

    LegacyProjectParts toLegacy(TesseraProject source) {
        Objects.requireNonNull(source, "source");
        Map<NodeId, GraphNodeReferenceDataObject> references =
                new LinkedHashMap<NodeId, GraphNodeReferenceDataObject>();
        for (NodeId nodeId : source.getNodes().keySet()) {
            references.put(nodeId, new GraphNodeReferenceDataObject(nodeId.getValue()));
        }

        Map<GraphNodeReferenceDataObject, NodeDataObject> nodes =
                new LinkedHashMap<GraphNodeReferenceDataObject, NodeDataObject>();
        Map<GraphNodeReferenceDataObject, NodeGlobalDataObject> globals =
                new LinkedHashMap<GraphNodeReferenceDataObject, NodeGlobalDataObject>();
        Map<GraphNodeReferenceDataObject, PipelineDataObject> pipelines =
                new LinkedHashMap<GraphNodeReferenceDataObject, PipelineDataObject>();

        for (ProjectNode node : source.getNodes().values()) {
            GraphNodeReferenceDataObject reference = references.get(node.getNodeId());
            nodes.put(reference, new NodeDataObject(
                    node.getNodeId().getValue(),
                    node.getId(),
                    node.getName(),
                    node.getDescription(),
                    node.getDownstream().stream().map(references::get).toList()
            ));
            globals.put(reference, new NodeGlobalDataObject(
                    node.getConfiguration().getStorages().stream()
                            .map(this::mapStorageToLegacy)
                            .toList()
            ));
            pipelines.put(reference, mapPipelineToLegacy(node.getPipeline()));
        }

        ProjectStructureDataObject structure = new ProjectStructureDataObject(
                new ProjectDataObject(source.getFormatVersion(), source.getName()),
                nodes
        );
        NodeContainerDataObject nodeContainer = new NodeContainerDataObject(
                structure,
                globals,
                pipelines
        );
        return LegacyProjectParts.newBuilder()
                .global(mapProjectConfigurationToLegacy(source.getConfiguration()))
                .nodeContainer(nodeContainer)
                .build();
    }

    List<DslFileDataObject> toLegacyDslSources(List<DslSource> sources) {
        return sources.stream()
                .map(source -> new DslFileDataObject(
                        new GraphNodeReferenceDataObject(source.getNodeId().getValue()),
                        source.getBaseName(),
                        source.getContent()
                ))
                .toList();
    }

    List<RoutineMetadata> toRoutineMetadata(
            List<RoutineEditorMetadataDataObject> source
    ) {
        return source.stream().map(metadata -> RoutineMetadata.newBuilder()
                .id(metadata.getRoutineId())
                .displayName(metadata.getDescriptor().getDisplayName())
                .description(text(metadata.getDescriptor().getDescription()))
                .version(metadata.getVersion())
                .artifact(metadata.getArtifact())
                .functions(metadata.getDescriptor().getFunctions().stream()
                        .map(this::mapRoutineFunction).toList())
                .build()).toList();
    }

    List<ServiceMetadata> toServiceMetadata(
            List<ServiceEditorMetadataDataObject> source
    ) {
        return source.stream().map(metadata -> ServiceMetadata.newBuilder()
                .id(metadata.getServiceId())
                .displayName(metadata.getDescriptor().getDisplayName())
                .description(text(metadata.getDescriptor().getDescription()))
                .version(metadata.getVersion())
                .artifact(metadata.getArtifact())
                .parameters(metadata.getDescriptor().getParameters().stream()
                        .map(parameter -> ServiceParameterMetadata.newBuilder()
                                .id(parameter.getParameterId())
                                .displayName(parameter.getDisplayName())
                                .description(text(parameter.getDescription()))
                                .type(ServiceParameterType.valueOf(
                                        parameter.getType().name()
                                ))
                                .defaultValue(parameter.getDefaultValue())
                                .selectOptions(parameter.getSelectOptions())
                                .storageRole(ServiceStorageRole.valueOf(
                                        parameter.getStorageRole().name()
                                ))
                                .build())
                        .toList())
                .build()).toList();
    }

    private ProjectConfiguration mapProjectConfiguration(ProjectGlobalDataObject source) {
        return ProjectConfiguration.newBuilder()
                .storages(source.getStorages().stream().map(this::mapStorage).toList())
                .services(source.getServices().stream().map(this::mapService).toList())
                .build();
    }

    private NodeConfiguration mapNodeConfiguration(NodeGlobalDataObject source) {
        return NodeConfiguration.newBuilder()
                .storages(source.getStorages().stream().map(this::mapStorage).toList())
                .build();
    }

    private StorageDefinition mapStorage(StorageDataObject source) {
        return StorageDefinition.newBuilder()
                .id(source.getIdName())
                .description(text(source.getDescription()))
                .options(source.getOptions().stream()
                        .map(option -> StorageOption.newBuilder()
                                .key(text(option.getKey()))
                                .value(text(option.getValue()))
                                .build())
                        .toList())
                .build();
    }

    private ServiceDefinition mapService(ServiceDataObject source) {
        return ServiceDefinition.newBuilder()
                .id(source.getIdName())
                .description(text(source.getDescription()))
                .options(source.getOptions().stream()
                        .map(option -> ServiceOption.newBuilder()
                                .name(text(option.getName()))
                                .data(text(option.getData()))
                                .build())
                        .toList())
                .build();
    }

    private Pipeline mapPipeline(PipelineDataObject source) {
        Map<String, StageDescriptionDataObject> descriptions =
                new LinkedHashMap<String, StageDescriptionDataObject>();
        for (StageDescriptionDataObject description : source.getStagesDescription()) {
            if (descriptions.put(description.getStageId(), description) != null) {
                throw new IllegalArgumentException(
                        "Duplicate pipeline stage description: " + description.getStageId()
                );
            }
        }

        List<PipelineStage> stages = new ArrayList<PipelineStage>();
        for (StageConsistencyDataObject consistency : source.getStagesConsistency()) {
            StageDescriptionDataObject description = descriptions.remove(consistency.getStageId());
            stages.add(PipelineStage.newBuilder()
                    .id(consistency.getStageId())
                    .position(Objects.requireNonNull(consistency.getPosition(), "stage position"))
                    .workers(description == null
                            ? List.of()
                            : description.getWorkers().stream().map(this::mapWorker).toList())
                    .build());
        }
        if (!descriptions.isEmpty()) {
            throw new IllegalArgumentException(
                    "Pipeline descriptions without positions: " + descriptions.keySet()
            );
        }
        stages.sort(Comparator.comparingInt(PipelineStage::getPosition));
        return Pipeline.newBuilder().stages(stages).build();
    }

    private Worker mapWorker(WorkerDescriptionDataObject source) {
        return Worker.newBuilder()
                .name(source.getName())
                .description(text(source.getDescription()))
                .configurationFiles(source.getConfigurationFiles().stream()
                        .map(file -> ConfigurationFile.newBuilder()
                                .id(file.getConfigurationFileId())
                                .description(text(file.getDescription()))
                                .build())
                        .toList())
                .build();
    }

    private ProjectGlobalDataObject mapProjectConfigurationToLegacy(
            ProjectConfiguration source
    ) {
        return new ProjectGlobalDataObject(
                source.getStorages().stream().map(this::mapStorageToLegacy).toList(),
                source.getServices().stream().map(this::mapServiceToLegacy).toList()
        );
    }

    private StorageDataObject mapStorageToLegacy(StorageDefinition source) {
        return new StorageDataObject(
                source.getOptions().stream()
                        .map(option -> new ConfigurationOptionDataObject(
                                option.getValue(),
                                option.getKey(),
                                null,
                                null
                        ))
                        .toList(),
                source.getDescription(),
                source.getId()
        );
    }

    private ServiceDataObject mapServiceToLegacy(ServiceDefinition source) {
        return new ServiceDataObject(
                source.getOptions().stream()
                        .map(option -> new ConfigurationOptionDataObject(
                                null,
                                null,
                                option.getData(),
                                option.getName()
                        ))
                        .toList(),
                source.getDescription(),
                source.getId()
        );
    }

    private PipelineDataObject mapPipelineToLegacy(Pipeline source) {
        List<PipelineStage> stages = source.getStages().stream()
                .sorted(Comparator.comparingInt(PipelineStage::getPosition))
                .toList();
        return new PipelineDataObject(
                stages.stream()
                        .map(stage -> new StageConsistencyDataObject(
                                stage.getId(),
                                stage.getPosition()
                        ))
                        .toList(),
                stages.stream()
                        .map(stage -> new StageDescriptionDataObject(
                                stage.getWorkers().stream()
                                        .map(this::mapWorkerToLegacy)
                                        .toList(),
                                stage.getId()
                        ))
                        .toList()
        );
    }

    private WorkerDescriptionDataObject mapWorkerToLegacy(Worker source) {
        return new WorkerDescriptionDataObject(
                source.getName(),
                source.getDescription(),
                source.getConfigurationFiles().stream()
                        .map(file -> new ConfigurationFileDataObject(
                                file.getDescription(),
                                file.getId()
                        ))
                        .toList()
        );
    }

    private RoutineFunction mapRoutineFunction(RoutineFunctionDescriptor source) {
        return RoutineFunction.newBuilder()
                .id(source.getFunctionId())
                .displayName(source.getDisplayName())
                .description(text(source.getDescription()))
                .widgetIds(source.getBduiWidgetIds())
                .argumentIds(source.getArgumentIds())
                .build();
    }

    private String text(String value) {
        return value == null ? "" : value;
    }

    private NodeId stableNodeId(NodeDataObject node) {
        return NodeId.newBuilder().value(node.getId() + ":" + node.getName()).build();
    }
}
