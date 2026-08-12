package io.github.byzatic.lib.configio.infrastructure.resource;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

public final class DelegatingClassLoaderComposite extends ClassLoader {

    private final List<ClassLoader> delegates;

    public DelegatingClassLoaderComposite(List<ClassLoader> delegates) {
        super(null);
        this.delegates = Collections.unmodifiableList(
                new ArrayList<ClassLoader>(delegates)
        );
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        for (ClassLoader delegate : delegates) {
            try {
                return delegate.loadClass(name);
            } catch (ClassNotFoundException ignored) {
            }
        }
        throw new ClassNotFoundException(name);
    }

    @Override
    public URL getResource(String name) {
        for (ClassLoader delegate : delegates) {
            URL resource = delegate.getResource(name);
            if (resource != null) {
                return resource;
            }
        }
        return null;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        Vector<URL> resources = new Vector<URL>();
        for (ClassLoader delegate : delegates) {
            resources.addAll(Collections.list(delegate.getResources(name)));
        }
        return resources.elements();
    }
}
