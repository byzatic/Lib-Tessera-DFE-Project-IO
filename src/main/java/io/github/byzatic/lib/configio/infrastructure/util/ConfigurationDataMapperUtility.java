package io.github.byzatic.lib.configio.infrastructure.util;

import io.github.byzatic.lib.configio.domain.model.ConfigurationFileDataObject;
import io.github.byzatic.lib.configio.domain.model.ConfigurationOptionDataObject;
import io.github.byzatic.lib.configio.domain.model.NodeGlobalDataObject;
import io.github.byzatic.lib.configio.domain.model.PipelineDataObject;
import io.github.byzatic.lib.configio.domain.model.ProjectGlobalDataObject;
import io.github.byzatic.lib.configio.domain.model.ServiceDataObject;
import io.github.byzatic.lib.configio.domain.model.StageConsistencyDataObject;
import io.github.byzatic.lib.configio.domain.model.StageDescriptionDataObject;
import io.github.byzatic.lib.configio.domain.model.StorageDataObject;
import io.github.byzatic.lib.configio.domain.model.WorkerDescriptionDataObject;
import io.github.byzatic.lib.configio.infrastructure.dto.raw.general.OptionsItem;
import io.github.byzatic.lib.configio.infrastructure.dto.raw.general.StoragesItem;
import io.github.byzatic.lib.configio.infrastructure.dto.raw.global.Global;
import io.github.byzatic.lib.configio.infrastructure.dto.raw.global.ServicesItem;
import io.github.byzatic.lib.configio.infrastructure.dto.raw.node.global.NodeGlobal;
import io.github.byzatic.lib.configio.infrastructure.dto.raw.node.pipeline.ConfigurationFilesItem;
import io.github.byzatic.lib.configio.infrastructure.dto.raw.node.pipeline.Pipeline;
import io.github.byzatic.lib.configio.infrastructure.dto.raw.node.pipeline.StagesConsistencyItem;
import io.github.byzatic.lib.configio.infrastructure.dto.raw.node.pipeline.StagesDescriptionItem;
import io.github.byzatic.lib.configio.infrastructure.dto.raw.node.pipeline.WorkersDescriptionItem;

import java.util.ArrayList;
import java.util.List;

public final class ConfigurationDataMapperUtility {

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
}
