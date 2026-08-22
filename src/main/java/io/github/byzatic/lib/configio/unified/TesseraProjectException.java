package io.github.byzatic.lib.configio.unified;

import java.nio.file.Path;
import java.util.Objects;

/** Checked failure reported at the unified project API boundary. */
public final class TesseraProjectException extends Exception {

    private final TesseraProjectOperation operation;
    private final Path location;

    /**
     * Creates an exception for a failed facade operation.
     *
     * @param operation operation that failed
     * @param location optional project or archive location involved in the failure
     * @param message human-readable failure description
     * @param cause original failure
     */
    public TesseraProjectException(
            TesseraProjectOperation operation,
            Path location,
            String message,
            Throwable cause
    ) {
        super(message, cause);
        this.operation = Objects.requireNonNull(operation, "operation");
        this.location = location == null ? null : location.toAbsolutePath().normalize();
    }

    /** @return operation that failed */
    public TesseraProjectOperation getOperation() {
        return operation;
    }

    /** @return normalized failure location, or {@code null} when not applicable */
    public Path getLocation() {
        return location;
    }
}
