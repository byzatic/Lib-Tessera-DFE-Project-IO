package io.github.byzatic.lib.configio.infrastructure.loader;

import io.github.byzatic.lib.configio.application.module.RoutineEditorMetadataLoaderInterface;
import io.github.byzatic.lib.configio.domain.exception.PluginLoadingException;
import io.github.byzatic.lib.configio.domain.model.RoutineEditorMetadataDataObject;
import io.github.byzatic.lib.configio.infrastructure.utils.ClassLoaderCloserUtility;
import io.github.cherepavel.tessera.configurator.routine.spi.RoutineEditorDescriptor;
import io.github.cherepavel.tessera.configurator.routine.spi.RoutineEditorDescriptorProvider;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/** Loads routine editor descriptors without creating executable routine instances. */
public final class RoutineEditorMetadataLoaderStrategy
        implements RoutineEditorMetadataLoaderInterface {

    private final Map<String, RoutineEditorMetadataDataObject> metadataByRoutineId;
    private final List<URLClassLoader> classLoaders;
    private final ClassLoaderCloserUtility classLoaderCloser;
    private boolean closed;

    public RoutineEditorMetadataLoaderStrategy(
            Path pluginsDirectory,
            ClassLoader sharedResourcesClassLoader
    ) throws PluginLoadingException {
        this(pluginsDirectory, sharedResourcesClassLoader, new ClassLoaderCloserUtility());
    }

    RoutineEditorMetadataLoaderStrategy(
            Path pluginsDirectory,
            ClassLoader sharedResourcesClassLoader,
            ClassLoaderCloserUtility classLoaderCloser
    ) throws PluginLoadingException {
        if (pluginsDirectory == null) {
            throw new PluginLoadingException("Plugins directory must not be null");
        }
        if (classLoaderCloser == null) {
            throw new IllegalArgumentException("classLoaderCloser must not be null");
        }
        this.metadataByRoutineId = new LinkedHashMap<String, RoutineEditorMetadataDataObject>();
        this.classLoaders = new ArrayList<URLClassLoader>();
        this.classLoaderCloser = classLoaderCloser;

        try {
            load(pluginsDirectory, sharedResourcesClassLoader);
        } catch (PluginLoadingException exception) {
            classLoaderCloser.closeAfterFailure(classLoaders, exception);
            throw exception;
        } catch (ServiceConfigurationError | LinkageError error) {
            PluginLoadingException failure = new PluginLoadingException(
                    "Cannot load routine editor metadata from: " + pluginsDirectory,
                    error
            );
            classLoaderCloser.closeAfterFailure(classLoaders, failure);
            throw failure;
        } catch (Exception exception) {
            PluginLoadingException failure = new PluginLoadingException(
                    "Cannot load routine editor metadata from: " + pluginsDirectory,
                    exception
            );
            classLoaderCloser.closeAfterFailure(classLoaders, failure);
            throw failure;
        }
    }

    @Override
    public synchronized List<RoutineEditorMetadataDataObject> getAvailableMetadata()
            throws PluginLoadingException {
        ensureOpen();
        return List.copyOf(metadataByRoutineId.values());
    }

    @Override
    public synchronized Optional<RoutineEditorMetadataDataObject> findMetadata(String routineId)
            throws PluginLoadingException {
        ensureOpen();
        if (routineId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(metadataByRoutineId.get(routineId.trim()));
    }

    @Override
    public synchronized void close() throws PluginLoadingException {
        if (closed) {
            return;
        }
        classLoaderCloser.close(classLoaders);
        closed = true;
    }

    private void load(Path pluginsDirectory, ClassLoader sharedResourcesClassLoader)
            throws Exception {
        File[] jarFiles = pluginsDirectory.toFile().listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File directory, String name) {
                return name.endsWith(".jar");
            }
        });
        if (jarFiles == null) {
            throw new PluginLoadingException(
                    "JAR files were not found in: " + pluginsDirectory
            );
        }
        Arrays.sort(jarFiles, Comparator.comparing(File::getName));

        for (File jarFile : jarFiles) {
            URLClassLoader classLoader = createClassLoader(jarFile, sharedResourcesClassLoader);
            classLoaders.add(classLoader);
            ServiceLoader<RoutineEditorDescriptorProvider> providers = ServiceLoader.load(
                    RoutineEditorDescriptorProvider.class,
                    classLoader
            );
            for (RoutineEditorDescriptorProvider provider : providers) {
                register(jarFile, provider.getDescriptor());
            }
        }
    }

    private URLClassLoader createClassLoader(
            File jarFile,
            ClassLoader sharedResourcesClassLoader
    ) throws Exception {
        ClassLoader parent = sharedResourcesClassLoader;
        if (parent == null) {
            parent = RoutineEditorDescriptorProvider.class.getClassLoader();
        }
        return new URLClassLoader(new URL[]{jarFile.toURI().toURL()}, parent);
    }

    private void register(File jarFile, RoutineEditorDescriptor descriptor)
            throws Exception {
        if (descriptor == null) {
            throw new PluginLoadingException(
                    "Routine editor descriptor must not be null in: " + jarFile
            );
        }
        String routineId = descriptor.getRoutineId();
        if (metadataByRoutineId.containsKey(routineId)) {
            throw new PluginLoadingException(
                    "Found another routine editor descriptor with id " + routineId
            );
        }
        metadataByRoutineId.put(
                routineId,
                new RoutineEditorMetadataDataObject(
                        jarFile.toPath(),
                        readImplementationVersion(jarFile),
                        descriptor
                )
        );
    }

    private String readImplementationVersion(File jarFile) throws Exception {
        try (JarFile archive = new JarFile(jarFile)) {
            Manifest manifest = archive.getManifest();
            if (manifest == null) {
                return "";
            }
            String version = manifest.getMainAttributes().getValue(
                    Attributes.Name.IMPLEMENTATION_VERSION
            );
            return version == null ? "" : version;
        }
    }

    private void ensureOpen() throws PluginLoadingException {
        if (closed) {
            throw new PluginLoadingException("Routine editor metadata loader is closed");
        }
    }
}
