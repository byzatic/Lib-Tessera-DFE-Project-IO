package io.github.byzatic.lib.configio.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ProjectGlobalDataObject {

    private final List<StorageDataObject> storages;
    private final List<ServiceDataObject> services;

    public ProjectGlobalDataObject(
            List<StorageDataObject> storages,
            List<ServiceDataObject> services
    ) {
        this.storages = immutableList(storages);
        this.services = immutableList(services);
    }

    public List<StorageDataObject> getStorages() {
        return storages;
    }

    public List<ServiceDataObject> getServices() {
        return services;
    }

    private <T> List<T> immutableList(List<T> source) {
        if (source == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<T>(source));
    }
}
