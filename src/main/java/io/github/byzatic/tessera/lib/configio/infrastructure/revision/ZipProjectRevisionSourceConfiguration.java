package io.github.byzatic.tessera.lib.configio.infrastructure.revision;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Objects;

/**
 * Immutable limits and paths used by {@link PollingZipProjectRevisionSource}.
 */
public final class ZipProjectRevisionSourceConfiguration {

    private final Path sourceArchive;
    private final Path stagingDirectory;
    private final Duration pollInterval;
    private final int stableObservationCount;
    private final int maximumEntryCount;
    private final long maximumExpandedBytes;

    private ZipProjectRevisionSourceConfiguration(Builder builder) {
        sourceArchive = builder.sourceArchive.toAbsolutePath().normalize();
        stagingDirectory = builder.stagingDirectory.toAbsolutePath().normalize();
        pollInterval = builder.pollInterval;
        stableObservationCount = builder.stableObservationCount;
        maximumEntryCount = builder.maximumEntryCount;
        maximumExpandedBytes = builder.maximumExpandedBytes;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Path getSourceArchive() {
        return sourceArchive;
    }

    public Path getStagingDirectory() {
        return stagingDirectory;
    }

    public Duration getPollInterval() {
        return pollInterval;
    }

    public int getStableObservationCount() {
        return stableObservationCount;
    }

    public int getMaximumEntryCount() {
        return maximumEntryCount;
    }

    public long getMaximumExpandedBytes() {
        return maximumExpandedBytes;
    }

    /**
     * Builds revision source configuration without exposing mutable state.
     */
    public static final class Builder {

        private Path sourceArchive;
        private Path stagingDirectory;
        private Duration pollInterval = Duration.ofSeconds(1L);
        private int stableObservationCount = 2;
        private int maximumEntryCount = 100000;
        private long maximumExpandedBytes = 1024L * 1024L * 1024L;

        private Builder() {
        }

        public Builder sourceArchive(Path value) {
            sourceArchive = value;
            return this;
        }

        public Builder stagingDirectory(Path value) {
            stagingDirectory = value;
            return this;
        }

        public Builder pollInterval(Duration value) {
            pollInterval = value;
            return this;
        }

        public Builder stableObservationCount(int value) {
            stableObservationCount = value;
            return this;
        }

        public Builder maximumEntryCount(int value) {
            maximumEntryCount = value;
            return this;
        }

        public Builder maximumExpandedBytes(long value) {
            maximumExpandedBytes = value;
            return this;
        }

        public ZipProjectRevisionSourceConfiguration build() {
            Objects.requireNonNull(sourceArchive, "sourceArchive");
            Objects.requireNonNull(stagingDirectory, "stagingDirectory");
            Objects.requireNonNull(pollInterval, "pollInterval");
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
            return new ZipProjectRevisionSourceConfiguration(this);
        }
    }
}
