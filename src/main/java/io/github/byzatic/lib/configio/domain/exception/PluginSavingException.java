package io.github.byzatic.lib.configio.domain.exception;

public class PluginSavingException extends Exception {

    public PluginSavingException(String message) {
        super(message);
    }

    public PluginSavingException(String message, Throwable cause) {
        super(message, cause);
    }
}
