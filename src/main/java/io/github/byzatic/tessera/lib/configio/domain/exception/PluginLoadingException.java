package io.github.byzatic.tessera.lib.configio.domain.exception;

public class PluginLoadingException extends Exception {

    public PluginLoadingException(String message) {
        super(message);
    }

    public PluginLoadingException(String message, Throwable cause) {
        super(message, cause);
    }

    public PluginLoadingException(Throwable cause) {
        super(cause);
    }
}
