package io.github.byzatic.tessera.lib.configio.domain.model;

public final class ConfigurationFileDataObject {

    private final String description;
    private final String configurationFileId;

    public ConfigurationFileDataObject(String description, String configurationFileId) {
        this.description = description;
        this.configurationFileId = configurationFileId;
    }

    public String getDescription() {
        return description;
    }

    public String getConfigurationFileId() {
        return configurationFileId;
    }
}
