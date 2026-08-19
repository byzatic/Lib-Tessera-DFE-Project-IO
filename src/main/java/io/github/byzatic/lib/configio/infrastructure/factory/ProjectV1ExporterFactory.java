package io.github.byzatic.lib.configio.infrastructure.factory;

import io.github.byzatic.lib.configio.application.saver.ProjectExporterInterface;
import io.github.byzatic.lib.configio.application.saver.ProjectV1ExporterStrategy;

public final class ProjectV1ExporterFactory {

    private ProjectV1ExporterFactory() {
    }

    public static ProjectExporterInterface create() {
        return new ProjectV1ExporterStrategy(ProjectV1SaverFactory.create());
    }
}
