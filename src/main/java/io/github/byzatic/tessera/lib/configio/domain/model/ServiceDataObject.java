package io.github.byzatic.tessera.lib.configio.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ServiceDataObject {

    private final List<ConfigurationOptionDataObject> options;
    private final String description;
    private final String idName;

    public ServiceDataObject(
            List<ConfigurationOptionDataObject> options,
            String description,
            String idName
    ) {
        if (options == null) {
            this.options = Collections.emptyList();
        } else {
            this.options = Collections.unmodifiableList(
                    new ArrayList<ConfigurationOptionDataObject>(options)
            );
        }
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
}
