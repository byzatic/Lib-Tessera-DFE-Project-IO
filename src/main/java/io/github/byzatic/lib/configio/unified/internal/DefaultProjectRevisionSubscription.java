package io.github.byzatic.lib.configio.unified.internal;

import io.github.byzatic.lib.configio.application.revision.ProjectRevisionSource;
import io.github.byzatic.lib.configio.unified.ProjectRevisionSubscription;

import java.util.Objects;

final class DefaultProjectRevisionSubscription implements ProjectRevisionSubscription {

    private final ProjectRevisionSource source;
    private boolean closed;

    DefaultProjectRevisionSubscription(ProjectRevisionSource source) {
        this.source = Objects.requireNonNull(source, "source");
    }

    @Override
    public synchronized boolean isClosed() {
        return closed;
    }

    @Override
    public synchronized void close() {
        if (closed) {
            return;
        }
        closed = true;
        source.close();
    }
}
