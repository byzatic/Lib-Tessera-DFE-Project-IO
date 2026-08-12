package io.github.byzatic.lib.configio.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class WorkerDescriptionDataObject {

    private final String name;
    private final String description;
    private final List<ConfigurationFileDataObject> configurationFiles;

    public WorkerDescriptionDataObject(
            String name,
            String description,
            List<ConfigurationFileDataObject> configurationFiles
    ) {
        this.name = name;
        this.description = description;
        if (configurationFiles == null) {
            this.configurationFiles = Collections.emptyList();
        } else {
            this.configurationFiles = Collections.unmodifiableList(
                    new ArrayList<ConfigurationFileDataObject>(configurationFiles)
            );
        }
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<ConfigurationFileDataObject> getConfigurationFiles() {
        return configurationFiles;
    }
}
