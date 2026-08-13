package io.github.byzatic.lib.configio.infrastructure.factory;

import io.github.byzatic.lib.configio.application.saver.ProjectSaverInterface;
import io.github.byzatic.lib.configio.application.saver.ProjectV1SaverStrategy;
import io.github.byzatic.lib.configio.infrastructure.dao.GsonNodeGlobalDao;
import io.github.byzatic.lib.configio.infrastructure.dao.GsonPipelineDao;
import io.github.byzatic.lib.configio.infrastructure.dao.GsonProjectDao;
import io.github.byzatic.lib.configio.infrastructure.dao.GsonProjectGlobalDao;

public final class ProjectV1SaverFactory {

    private ProjectV1SaverFactory() {
    }

    public static ProjectSaverInterface create() {
        return new ProjectV1SaverStrategy(
                new GsonProjectDao(),
                new GsonProjectGlobalDao(),
                new GsonNodeGlobalDao(),
                new GsonPipelineDao()
        );
    }
}
