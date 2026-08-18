package io.github.byzatic.lib.configio.infrastructure.factory;

import io.github.byzatic.lib.configio.application.loader.ProjectLoaderInterface;
import io.github.byzatic.lib.configio.application.revision.ProjectRevisionSource;
import io.github.byzatic.lib.configio.infrastructure.revision.PollingZipProjectRevisionSource;
import io.github.byzatic.lib.configio.infrastructure.revision.ZipProjectRevisionSourceConfiguration;

import java.util.Objects;

/**
 * Creates project revision sources backed by the standard config-io project loader.
 */
public final class ProjectRevisionSourceFactory {

    private ProjectRevisionSourceFactory() {
    }

    /**
     * Creates a polling ZIP revision source.
     *
     * @param configuration source paths, polling policy, and extraction limits
     * @return a new source which has not been started
     */
    public static ProjectRevisionSource create(
            ZipProjectRevisionSourceConfiguration configuration
    ) {
        Objects.requireNonNull(configuration, "configuration");
        ProjectLoaderInterface loader = ProjectV1LoaderFactory.create();
        return new PollingZipProjectRevisionSource(configuration, loader);
    }
}
