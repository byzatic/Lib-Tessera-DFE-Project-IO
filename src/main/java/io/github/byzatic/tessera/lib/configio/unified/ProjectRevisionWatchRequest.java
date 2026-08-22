package io.github.byzatic.tessera.lib.configio.unified;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Objects;

/** Immutable polling and extraction policy for watching a project ZIP. */
public final class ProjectRevisionWatchRequest {

    private final Path sourceArchive;
    private final Path stagingDirectory;
    private final Duration pollInterval;
    private final int stableObservationCount;
    private final int maximumEntryCount;
    private final long maximumExpandedBytes;

    private ProjectRevisionWatchRequest(Builder builder) {
        sourceArchive = builder.sourceArchive.toAbsolutePath().normalize();
        stagingDirectory = builder.stagingDirectory.toAbsolutePath().normalize();
        pollInterval = builder.pollInterval;
        stableObservationCount = builder.stableObservationCount;
        maximumEntryCount = builder.maximumEntryCount;
        maximumExpandedBytes = builder.maximumExpandedBytes;
    }

    /**
     * Creates a revision-watch request builder.
     *
     * @param sourceArchive ZIP archive whose revisions are monitored
     * @param stagingDirectory directory used to extract stable revisions
     * @return a new request builder
     */
    public static Builder builder(Path sourceArchive, Path stagingDirectory) {
        return new Builder(sourceArchive, stagingDirectory);
    }

    /** @return normalized absolute path to the monitored ZIP archive */
    public Path getSourceArchive() {
        return sourceArchive;
    }

    /** @return normalized absolute path to the extraction directory */
    public Path getStagingDirectory() {
        return stagingDirectory;
    }

    /** @return interval between source archive observations */
    public Duration getPollInterval() {
        return pollInterval;
    }

    /** @return number of unchanged observations required before loading a revision */
    public int getStableObservationCount() {
        return stableObservationCount;
    }

    /** @return maximum number of entries accepted from a revision archive */
    public int getMaximumEntryCount() {
        return maximumEntryCount;
    }

    /** @return maximum total uncompressed size accepted from a revision archive */
    public long getMaximumExpandedBytes() {
        return maximumExpandedBytes;
    }

    /** Builder with conservative polling and ZIP extraction defaults. */
    public static final class Builder {

        private final Path sourceArchive;
        private final Path stagingDirectory;
        private Duration pollInterval = Duration.ofSeconds(1L);
        private int stableObservationCount = 2;
        private int maximumEntryCount = 100000;
        private long maximumExpandedBytes = 1024L * 1024L * 1024L;

        private Builder(Path sourceArchive, Path stagingDirectory) {
            this.sourceArchive = Objects.requireNonNull(sourceArchive, "sourceArchive");
            this.stagingDirectory = Objects.requireNonNull(stagingDirectory, "stagingDirectory");
        }

        /**
         * Sets the interval between archive observations.
         *
         * @param value positive polling interval
         * @return this builder
         */
        public Builder pollInterval(Duration value) {
            pollInterval = Objects.requireNonNull(value, "pollInterval");
            return this;
        }

        /**
         * Sets how many unchanged observations make a revision stable.
         *
         * @param value positive observation count
         * @return this builder
         */
        public Builder stableObservationCount(int value) {
            stableObservationCount = value;
            return this;
        }

        /**
         * Sets the archive entry-count safety limit.
         *
         * @param value positive maximum entry count
         * @return this builder
         */
        public Builder maximumEntryCount(int value) {
            maximumEntryCount = value;
            return this;
        }

        /**
         * Sets the total expanded-size safety limit.
         *
         * @param value positive byte limit
         * @return this builder
         */
        public Builder maximumExpandedBytes(long value) {
            maximumExpandedBytes = value;
            return this;
        }

        /**
         * Validates the configured limits and creates an immutable request.
         *
         * @return configured request
         * @throws IllegalArgumentException if any numeric limit is not positive
         */
        public ProjectRevisionWatchRequest build() {
            if (pollInterval.isZero() || pollInterval.isNegative()) {
                throw new IllegalArgumentException("pollInterval must be positive");
            }
            if (stableObservationCount < 1) {
                throw new IllegalArgumentException("stableObservationCount must be positive");
            }
            if (maximumEntryCount < 1) {
                throw new IllegalArgumentException("maximumEntryCount must be positive");
            }
            if (maximumExpandedBytes < 1L) {
                throw new IllegalArgumentException("maximumExpandedBytes must be positive");
            }
            return new ProjectRevisionWatchRequest(this);
        }
    }
}
