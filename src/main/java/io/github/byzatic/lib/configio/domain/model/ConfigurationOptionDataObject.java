package io.github.byzatic.lib.configio.domain.model;

public final class ConfigurationOptionDataObject {

    private final String value;
    private final String key;
    private final String data;
    private final String name;

    public ConfigurationOptionDataObject(String value, String key, String data, String name) {
        this.value = value;
        this.key = key;
        this.data = data;
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public String getKey() {
        return key;
    }

    public String getData() {
        return data;
    }

    public String getName() {
        return name;
    }
}
