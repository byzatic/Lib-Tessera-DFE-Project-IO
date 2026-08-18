package io.github.byzatic.lib.configio.infrastructure.utils;

import io.github.byzatic.lib.configio.domain.exception.PluginLoadingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class JarPluginLoaderUtility {

    private static final Logger LOGGER = LoggerFactory.getLogger(JarPluginLoaderUtility.class);

    public <F> void loadFactories(
            Path pluginsDirectory,
            ClassLoader sharedResourcesClassLoader,
            Class<F> factoryClass,
            String pluginKind,
            Map<String, F> factories,
            List<URLClassLoader> classLoaders
    ) throws PluginLoadingException {
        if (pluginsDirectory == null) {
            throw new PluginLoadingException("Plugins directory must not be null");
        }
        if (factoryClass == null) {
            throw new PluginLoadingException("Plugin factory class must not be null");
        }

        try {
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

            for (File jarFile : jarFiles) {
                LOGGER.debug("Found JAR: {}", jarFile);
                URL[] urls = new URL[]{jarFile.toURI().toURL()};
                traceClasses(urls);

                ClassLoader parent = sharedResourcesClassLoader;
                if (parent == null) {
                    parent = factoryClass.getClassLoader();
                }
                URLClassLoader classLoader = new URLClassLoader(urls, parent);
                classLoaders.add(classLoader);

                ServiceLoader<F> discoveredFactories = ServiceLoader.load(
                        factoryClass,
                        classLoader
                );
                for (F discoveredFactory : discoveredFactories) {
                    String pluginName = discoveredFactory.getClass()
                            .getSimpleName()
                            .replace("Factory", "");
                    LOGGER.debug("Discovered {}: {}", pluginKind, pluginName);

                    if (factories.containsKey(pluginName)) {
                        throw new PluginLoadingException(
                                "Found another " + pluginKind + " with name " + pluginName
                                        + " (" + pluginKind + " duplication)"
                        );
                    }

                    F factory = factoryClass.cast(
                            discoveredFactory.getClass().getDeclaredConstructor().newInstance()
                    );
                    factories.put(pluginName, factory);
                }
            }
        } catch (PluginLoadingException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new PluginLoadingException(
                    "Cannot load " + pluginKind + " plugins from: " + pluginsDirectory,
                    exception
            );
        }
    }

    private void traceClasses(URL[] urls) throws Exception {
        if (!LOGGER.isTraceEnabled()) {
            return;
        }
        for (URL url : urls) {
            LOGGER.trace("Loading from URL: {}", url);
            try (JarFile jarFile = new JarFile(new File(url.toURI()))) {
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (entry.getName().endsWith(".class")) {
                        LOGGER.trace("Class found: {}", entry.getName());
                    }
                }
            }
        }
    }
}
