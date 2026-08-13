package io.github.byzatic.lib.configio.domain.exception;

public class ProjectSavingException extends Exception {

    public ProjectSavingException(String message) {
        super(message);
    }

    public ProjectSavingException(String message, Throwable cause) {
        super(message, cause);
    }
}
