package io.github.byzatic.lib.configio.domain.exception;

public class ProjectLoadingException extends Exception {

    public ProjectLoadingException(String message) {
        super(message);
    }

    public ProjectLoadingException(String message, Throwable cause) {
        super(message, cause);
    }
}
