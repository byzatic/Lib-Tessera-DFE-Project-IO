package io.github.byzatic.lib.configio.infrastructure.dao;

import io.github.byzatic.lib.configio.application.dao.SharedResourcesDaoInterface;
import io.github.byzatic.lib.configio.domain.exception.ProjectLoadingException;
import io.github.byzatic.lib.configio.domain.model.SharedResourcesContainerDataObject;
import io.github.byzatic.lib.configio.infrastructure.resource.DelegatingClassLoaderComposite;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class UrlClassLoaderSharedResourcesDao implements SharedResourcesDaoInterface {

    private final List<ClassLoader> preloadedClassLoaders;

    public UrlClassLoaderSharedResourcesDao() {
        this(Collections.<ClassLoader>emptyList());
    }

    public UrlClassLoaderSharedResourcesDao(List<ClassLoader> preloadedClassLoaders) {
        if (preloadedClassLoaders == null) {
            throw new IllegalArgumentException("preloadedClassLoaders must not be null");
        }
        this.preloadedClassLoaders = Collections.unmodifiableList(
                new ArrayList<ClassLoader>(preloadedClassLoaders)
        );
    }

    @Override
    public SharedResourcesContainerDataObject load(Path projectDirectory)
            throws ProjectLoadingException {
        Path sharedDirectory = projectDirectory.resolve("modules").resolve("shared");
        if (!Files.isDirectory(sharedDirectory)) {
            throw new ProjectLoadingException(
                    "Shared resources directory does not exist: " + sharedDirectory
            );
        }

        List<Path> jarFiles = listJarFiles(sharedDirectory);
        List<ClassLoader> classLoaders = new ArrayList<ClassLoader>();
        List<URLClassLoader> ownedClassLoaders = new ArrayList<URLClassLoader>();
        ClassLoader parent = createInitialParent(classLoaders);

        try {
            for (Path jarFile : jarFiles) {
                URLClassLoader classLoader = new URLClassLoader(
                        new URL[]{jarFile.toUri().toURL()},
                        parent
                );
                ownedClassLoaders.add(classLoader);
                classLoaders.add(classLoader);
                parent = classLoader;
            }
            return new SharedResourcesContainerDataObject(classLoaders, ownedClassLoaders);
        } catch (Exception exception) {
            closeOwnedClassLoaders(ownedClassLoaders, exception);
            throw new ProjectLoadingException(
                    "Cannot load shared resources from: " + sharedDirectory,
                    exception
            );
        }
    }

    private List<Path> listJarFiles(Path sharedDirectory)
            throws ProjectLoadingException {
        List<Path> jarFiles = new ArrayList<Path>();
        try (DirectoryStream<Path> entries = Files.newDirectoryStream(sharedDirectory, "*.jar")) {
            for (Path entry : entries) {
                if (Files.isRegularFile(entry)) {
                    jarFiles.add(entry.toAbsolutePath().normalize());
                }
            }
        } catch (IOException exception) {
            throw new ProjectLoadingException(
                    "Cannot list shared resources: " + sharedDirectory,
                    exception
            );
        }
        Collections.sort(jarFiles);
        return jarFiles;
    }

    private ClassLoader createInitialParent(List<ClassLoader> classLoaders) {
        if (preloadedClassLoaders.isEmpty()) {
            return ClassLoader.getSystemClassLoader();
        }
        DelegatingClassLoaderComposite composite =
                new DelegatingClassLoaderComposite(preloadedClassLoaders);
        classLoaders.add(composite);
        return composite;
    }

    private void closeOwnedClassLoaders(
            List<URLClassLoader> classLoaders,
            Throwable failure
    ) {
        for (int index = classLoaders.size() - 1; index >= 0; index--) {
            try {
                classLoaders.get(index).close();
            } catch (IOException exception) {
                failure.addSuppressed(exception);
            }
        }
    }
}
