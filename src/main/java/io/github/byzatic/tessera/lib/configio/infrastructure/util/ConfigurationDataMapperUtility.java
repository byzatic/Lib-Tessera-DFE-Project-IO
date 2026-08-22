package io.github.byzatic.tessera.lib.configio.infrastructure.util;

import io.github.byzatic.tessera.lib.configio.domain.model.ConfigurationFileDataObject;
import io.github.byzatic.tessera.lib.configio.domain.model.ConfigurationOptionDataObject;
import io.github.byzatic.tessera.lib.configio.domain.model.NodeGlobalDataObject;
import io.github.byzatic.tessera.lib.configio.domain.model.PipelineDataObject;
import io.github.byzatic.tessera.lib.configio.domain.model.ProjectGlobalDataObject;
import io.github.byzatic.tessera.lib.configio.domain.model.ServiceDataObject;
import io.github.byzatic.tessera.lib.configio.domain.model.StageConsistencyDataObject;
import io.github.byzatic.tessera.lib.configio.domain.model.StageDescriptionDataObject;
import io.github.byzatic.tessera.lib.configio.domain.model.StorageDataObject;
import io.github.byzatic.tessera.lib.configio.domain.model.WorkerDescriptionDataObject;
import io.github.byzatic.tessera.lib.configio.infrastructure.dto.raw.general.OptionsItem;
import io.github.byzatic.tessera.lib.configio.infrastructure.dto.raw.general.StoragesItem;
import io.github.byzatic.tessera.lib.configio.infrastructure.dto.raw.global.Global;
import io.github.byzatic.tessera.lib.configio.infrastructure.dto.raw.global.ServicesItem;
import io.github.byzatic.tessera.lib.configio.infrastructure.dto.raw.node.global.NodeGlobal;
import io.github.byzatic.tessera.lib.configio.infrastructure.dto.raw.node.pipeline.ConfigurationFilesItem;
import io.github.byzatic.tessera.lib.configio.infrastructure.dto.raw.node.pipeline.Pipeline;
import io.github.byzatic.tessera.lib.configio.infrastructure.dto.raw.node.pipeline.StagesConsistencyItem;
import io.github.byzatic.tessera.lib.configio.infrastructure.dto.raw.node.pipeline.StagesDescriptionItem;
import io.github.byzatic.tessera.lib.configio.infrastructure.dto.raw.node.pipeline.WorkersDescriptionItem;

import java.util.ArrayList;
import java.util.List;

public final class ConfigurationDataMapperUtility {

    public Global mapProjectGlobal(ProjectGlobalDataObject source) {
        List<ServicesItem> services = new ArrayList<ServicesItem>();
        for (ServiceDataObject service : source.getServices()) {
            services.add(ServicesItem.newBuilder()
                    .setOptions(mapOptionItems(service.getOptions()))
                    .setDescription(service.getDescription())
                    .setIdName(service.getIdName())
                    .build());
        }
        return Global.newBuilder()
                .setStorages(mapStorageItems(source.getStorages()))
                .setServices(services)
                .build();
    }

    public NodeGlobal mapNodeGlobal(NodeGlobalDataObject source) {
        return NodeGlobal.newBuilder()
                .setStorages(mapStorageItems(source.getStorages()))
                .build();
    }

    public Pipeline mapPipeline(PipelineDataObject source) {
        List<StagesConsistencyItem> consistency =
                new ArrayList<StagesConsistencyItem>();
        for (StageConsistencyDataObject stage : source.getStagesConsistency()) {
            consistency.add(StagesConsistencyItem.newBuilder()
                    .setStageId(stage.getStageId())
                    .setPosition(stage.getPosition())
                    .build());
        }

        List<StagesDescriptionItem> descriptions =
                new ArrayList<StagesDescriptionItem>();
        for (StageDescriptionDataObject stage : source.getStagesDescription()) {
            descriptions.add(StagesDescriptionItem.newBuilder()
                    .setWorkersDescription(mapWorkerItems(stage.getWorkers()))
                    .setStageId(stage.getStageId())
                    .build());
        }
        return Pipeline.newBuilder()
                .setStagesConsistency(consistency)
                .setStagesDescription(descriptions)
                .build();
    }

    public ProjectGlobalDataObject mapProjectGlobal(Global source) {
        List<StorageDataObject> storages = mapStorages(source.getStorages());
        List<ServiceDataObject> services = new ArrayList<ServiceDataObject>();
        if (source.getServices() != null) {
            for (ServicesItem service : source.getServices()) {
                services.add(new ServiceDataObject(
                        mapOptions(service.getOptions()),
                        service.getDescription(),
                        service.getIdName()
                ));
            }
        }
        return new ProjectGlobalDataObject(storages, services);
    }

    public NodeGlobalDataObject mapNodeGlobal(NodeGlobal source) {
        return new NodeGlobalDataObject(mapStorages(source.getStorages()));
    }

    public PipelineDataObject mapPipeline(Pipeline source) {
        List<StageConsistencyDataObject> consistency =
                new ArrayList<StageConsistencyDataObject>();
        if (source.getStagesConsistency() != null) {
            for (StagesConsistencyItem stage : source.getStagesConsistency()) {
                consistency.add(new StageConsistencyDataObject(
                        stage.getStageId(),
                        stage.getPosition()
                ));
            }
        }

        List<StageDescriptionDataObject> descriptions =
                new ArrayList<StageDescriptionDataObject>();
        if (source.getStagesDescription() != null) {
            for (StagesDescriptionItem stage : source.getStagesDescription()) {
                descriptions.add(new StageDescriptionDataObject(
                        mapWorkers(stage.getWorkersDescription()),
                        stage.getStageId()
                ));
            }
        }
        return new PipelineDataObject(consistency, descriptions);
    }

    private List<StorageDataObject> mapStorages(List<StoragesItem> source) {
        List<StorageDataObject> storages = new ArrayList<StorageDataObject>();
        if (source != null) {
            for (StoragesItem storage : source) {
                storages.add(new StorageDataObject(
                        mapOptions(storage.getOptions()),
                        storage.getDescription(),
                        storage.getIdName()
                ));
            }
        }
        return storages;
    }

    private List<StoragesItem> mapStorageItems(List<StorageDataObject> source) {
        List<StoragesItem> storages = new ArrayList<StoragesItem>();
        for (StorageDataObject storage : source) {
            storages.add(StoragesItem.newBuilder()
                    .setOptions(mapOptionItems(storage.getOptions()))
                    .setDescription(storage.getDescription())
                    .setIdName(storage.getIdName())
                    .build());
        }
        return storages;
    }

    private List<ConfigurationOptionDataObject> mapOptions(List<OptionsItem> source) {
        List<ConfigurationOptionDataObject> options =
                new ArrayList<ConfigurationOptionDataObject>();
        if (source != null) {
            for (OptionsItem option : source) {
                options.add(new ConfigurationOptionDataObject(
                        option.getValue(),
                        option.getKey(),
                        option.getData(),
                        option.getName()
                ));
            }
        }
        return options;
    }

    private List<OptionsItem> mapOptionItems(List<ConfigurationOptionDataObject> source) {
        List<OptionsItem> options = new ArrayList<OptionsItem>();
        for (ConfigurationOptionDataObject option : source) {
            options.add(OptionsItem.newBuilder()
                    .setValue(option.getValue())
                    .setKey(option.getKey())
                    .setData(option.getData())
                    .setName(option.getName())
                    .build());
        }
        return options;
    }

    private List<WorkerDescriptionDataObject> mapWorkers(
            List<WorkersDescriptionItem> source
    ) {
        List<WorkerDescriptionDataObject> workers =
                new ArrayList<WorkerDescriptionDataObject>();
        if (source != null) {
            for (WorkersDescriptionItem worker : source) {
                workers.add(new WorkerDescriptionDataObject(
                        worker.getName(),
                        worker.getDescription(),
                        mapConfigurationFiles(worker.getConfigurationFiles())
                ));
            }
        }
        return workers;
    }

    private List<WorkersDescriptionItem> mapWorkerItems(
            List<WorkerDescriptionDataObject> source
    ) {
        List<WorkersDescriptionItem> workers = new ArrayList<WorkersDescriptionItem>();
        for (WorkerDescriptionDataObject worker : source) {
            workers.add(WorkersDescriptionItem.newBuilder()
                    .setName(worker.getName())
                    .setDescription(worker.getDescription())
                    .setConfigurationFiles(mapConfigurationFileItems(
                            worker.getConfigurationFiles()
                    ))
                    .build());
        }
        return workers;
    }

    private List<ConfigurationFileDataObject> mapConfigurationFiles(
            List<ConfigurationFilesItem> source
    ) {
        List<ConfigurationFileDataObject> files =
                new ArrayList<ConfigurationFileDataObject>();
        if (source != null) {
            for (ConfigurationFilesItem file : source) {
                files.add(new ConfigurationFileDataObject(
                        file.getDescription(),
                        file.getConfigurationFileId()
                ));
            }
        }
        return files;
    }

    private List<ConfigurationFilesItem> mapConfigurationFileItems(
            List<ConfigurationFileDataObject> source
    ) {
        List<ConfigurationFilesItem> files = new ArrayList<ConfigurationFilesItem>();
        for (ConfigurationFileDataObject file : source) {
            files.add(ConfigurationFilesItem.newBuilder()
                    .setDescription(file.getDescription())
                    .setConfigurationFileId(file.getConfigurationFileId())
                    .build());
        }
        return files;
    }
}
