package io.github.byzatic.lib.configio.infrastructure.factory;

import io.github.byzatic.lib.configio.application.loader.ProjectLoaderInterface;
import io.github.byzatic.lib.configio.application.loader.ProjectV1LoaderStrategy;
import io.github.byzatic.lib.configio.infrastructure.dao.GsonNodeGlobalDao;
import io.github.byzatic.lib.configio.infrastructure.dao.GsonPipelineDao;
import io.github.byzatic.lib.configio.infrastructure.dao.GsonProjectDao;
import io.github.byzatic.lib.configio.infrastructure.dao.GsonProjectGlobalDao;
import io.github.byzatic.lib.configio.infrastructure.dao.UrlClassLoaderSharedResourcesDao;

import java.util.Collections;
import java.util.List;

public final class ProjectV1LoaderFactory {

    private ProjectV1LoaderFactory() {
    }

    public static ProjectLoaderInterface create() {
        return create(Collections.<ClassLoader>emptyList());
    }

    public static ProjectLoaderInterface create(List<ClassLoader> preloadedClassLoaders) {
        return new ProjectV1LoaderStrategy(
                new GsonProjectDao(),
                new GsonProjectGlobalDao(),
                new GsonNodeGlobalDao(),
                new GsonPipelineDao(),
                new UrlClassLoaderSharedResourcesDao(preloadedClassLoaders)
        );
    }
}
