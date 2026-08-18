package io.github.byzatic.lib.configio.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class StorageDataObject {

    private final List<ConfigurationOptionDataObject> options;
    private final String description;
    private final String idName;

    public StorageDataObject(
            List<ConfigurationOptionDataObject> options,
            String description,
            String idName
    ) {
        this.options = immutableOptions(options);
        this.description = description;
        this.idName = idName;
    }

    public List<ConfigurationOptionDataObject> getOptions() {
        return options;
    }

    public String getDescription() {
        return description;
    }

    public String getIdName() {
        return idName;
    }

    private List<ConfigurationOptionDataObject> immutableOptions(
            List<ConfigurationOptionDataObject> source
    ) {
        if (source == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(
                new ArrayList<ConfigurationOptionDataObject>(source)
        );
    }
}
