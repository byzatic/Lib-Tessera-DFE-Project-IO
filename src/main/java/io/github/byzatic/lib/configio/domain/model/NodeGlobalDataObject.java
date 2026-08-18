package io.github.byzatic.lib.configio.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class NodeGlobalDataObject {

    private final List<StorageDataObject> storages;

    public NodeGlobalDataObject(List<StorageDataObject> storages) {
        if (storages == null) {
            this.storages = Collections.emptyList();
        } else {
            this.storages = Collections.unmodifiableList(
                    new ArrayList<StorageDataObject>(storages)
            );
        }
    }

    public List<StorageDataObject> getStorages() {
        return storages;
    }
}
