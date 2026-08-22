package io.github.byzatic.tessera.lib.configio.infrastructure.utils;

import io.github.byzatic.tessera.lib.configio.domain.exception.PluginLoadingException;

import java.io.IOException;
import java.net.URLClassLoader;
import java.util.List;

public final class ClassLoaderCloserUtility {

    public void close(List<URLClassLoader> classLoaders) throws PluginLoadingException {
        IOException failure = null;
        for (int index = classLoaders.size() - 1; index >= 0; index--) {
            try {
                classLoaders.get(index).close();
            } catch (IOException exception) {
                if (failure == null) {
                    failure = exception;
                } else {
                    failure.addSuppressed(exception);
                }
            }
        }
        if (failure != null) {
            throw new PluginLoadingException("Cannot close plugin class loaders", failure);
        }
    }

    public void closeAfterFailure(List<URLClassLoader> classLoaders, Throwable failure) {
        for (int index = classLoaders.size() - 1; index >= 0; index--) {
            try {
                classLoaders.get(index).close();
            } catch (IOException exception) {
                failure.addSuppressed(exception);
            }
        }
    }
}
