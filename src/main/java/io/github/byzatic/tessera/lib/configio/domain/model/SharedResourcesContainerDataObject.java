package io.github.byzatic.tessera.lib.configio.domain.model;

import java.io.IOException;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SharedResourcesContainerDataObject implements AutoCloseable {

    private final List<ClassLoader> classLoaders;
    private final List<URLClassLoader> ownedClassLoaders;
    private boolean closed;

    public SharedResourcesContainerDataObject(
            List<ClassLoader> classLoaders,
            List<URLClassLoader> ownedClassLoaders
    ) {
        this.classLoaders = Collections.unmodifiableList(
                new ArrayList<ClassLoader>(classLoaders)
        );
        this.ownedClassLoaders = new ArrayList<URLClassLoader>(ownedClassLoaders);
    }

    public List<ClassLoader> getClassLoaders() {
        return classLoaders;
    }

    public ClassLoader getLastClassLoader() {
        if (classLoaders.isEmpty()) {
            return null;
        }
        return classLoaders.get(classLoaders.size() - 1);
    }

    public boolean isClosed() {
        return closed;
    }

    @Override
    public synchronized void close() throws IOException {
        if (closed) {
            return;
        }

        IOException failure = null;
        for (int index = ownedClassLoaders.size() - 1; index >= 0; index--) {
            try {
                ownedClassLoaders.get(index).close();
            } catch (IOException exception) {
                if (failure == null) {
                    failure = exception;
                } else {
                    failure.addSuppressed(exception);
                }
            }
        }
        closed = true;

        if (failure != null) {
            throw failure;
        }
    }
}
