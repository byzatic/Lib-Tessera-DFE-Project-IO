package io.github.byzatic.lib.configio.unified;

/** Running project revision observation owned by the caller. */
public interface ProjectRevisionSubscription extends AutoCloseable {

    /** Returns whether revision observation has been stopped. */
    boolean isClosed();

    /** Stops revision observation and releases its executor. */
    @Override
    void close();
}
