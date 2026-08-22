package io.github.byzatic.tessera.lib.configio.infrastructure.loader;

import io.github.byzatic.tessera.lib.configio.application.service.ServiceEditorMetadataLoaderInterface;
import io.github.byzatic.tessera.lib.configio.domain.exception.PluginLoadingException;
import io.github.byzatic.tessera.lib.configio.domain.model.ServiceEditorMetadataDataObject;
import io.github.byzatic.tessera.lib.configio.infrastructure.utils.ClassLoaderCloserUtility;
import io.github.byzatic.tessera.lib.configio.service_spi.ServiceEditorDescriptor;
import io.github.byzatic.tessera.lib.configio.service_spi.ServiceEditorDescriptorProvider;

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

/** Loads service editor descriptors without creating executable service instances. */
public final class ServiceEditorMetadataLoaderStrategy
        implements ServiceEditorMetadataLoaderInterface {

    private final Map<String, ServiceEditorMetadataDataObject> metadataByServiceId;
    private final List<URLClassLoader> classLoaders;
    private final ClassLoaderCloserUtility classLoaderCloser;
    private boolean closed;

    public ServiceEditorMetadataLoaderStrategy(
            Path pluginsDirectory,
            ClassLoader sharedResourcesClassLoader
    ) throws PluginLoadingException {
        this(pluginsDirectory, sharedResourcesClassLoader, new ClassLoaderCloserUtility());
    }

    ServiceEditorMetadataLoaderStrategy(
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
        this.metadataByServiceId =
                new LinkedHashMap<String, ServiceEditorMetadataDataObject>();
        this.classLoaders = new ArrayList<URLClassLoader>();
        this.classLoaderCloser = classLoaderCloser;

        try {
            load(pluginsDirectory, sharedResourcesClassLoader);
        } catch (PluginLoadingException exception) {
            classLoaderCloser.closeAfterFailure(classLoaders, exception);
            throw exception;
        } catch (ServiceConfigurationError | LinkageError error) {
            PluginLoadingException failure = new PluginLoadingException(
                    "Cannot load service editor metadata from: " + pluginsDirectory,
                    error
            );
            classLoaderCloser.closeAfterFailure(classLoaders, failure);
            throw failure;
        } catch (Exception exception) {
            PluginLoadingException failure = new PluginLoadingException(
                    "Cannot load service editor metadata from: " + pluginsDirectory,
                    exception
            );
            classLoaderCloser.closeAfterFailure(classLoaders, failure);
            throw failure;
        }
    }

    @Override
    public synchronized List<ServiceEditorMetadataDataObject> getAvailableMetadata()
            throws PluginLoadingException {
        ensureOpen();
        return List.copyOf(metadataByServiceId.values());
    }

    @Override
    public synchronized Optional<ServiceEditorMetadataDataObject> findMetadata(String serviceId)
            throws PluginLoadingException {
        ensureOpen();
        if (serviceId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(metadataByServiceId.get(serviceId.trim()));
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
            ServiceLoader<ServiceEditorDescriptorProvider> providers = ServiceLoader.load(
                    ServiceEditorDescriptorProvider.class,
                    classLoader
            );
            for (ServiceEditorDescriptorProvider provider : providers) {
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
            parent = ServiceEditorDescriptorProvider.class.getClassLoader();
        }
        return new URLClassLoader(new URL[]{jarFile.toURI().toURL()}, parent);
    }

    private void register(File jarFile, ServiceEditorDescriptor descriptor)
            throws Exception {
        if (descriptor == null) {
            throw new PluginLoadingException(
                    "Service editor descriptor must not be null in: " + jarFile
            );
        }
        String serviceId = descriptor.getServiceId();
        if (metadataByServiceId.containsKey(serviceId)) {
            throw new PluginLoadingException(
                    "Found another service editor descriptor with id " + serviceId
            );
        }
        metadataByServiceId.put(
                serviceId,
                new ServiceEditorMetadataDataObject(
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
            throw new PluginLoadingException("Service editor metadata loader is closed");
        }
    }
}
