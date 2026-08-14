package io.github.byzatic.lib.configio.domain.exception;

/**
 * Reports a failure while detecting, staging, extracting, or loading a project revision.
 */
public class ProjectRevisionException extends Exception {

    public ProjectRevisionException(String message) {
        super(message);
    }

    public ProjectRevisionException(String message, Throwable cause) {
        super(message, cause);
    }
}
