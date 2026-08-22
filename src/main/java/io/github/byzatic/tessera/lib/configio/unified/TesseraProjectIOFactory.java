package io.github.byzatic.tessera.lib.configio.unified;

import io.github.byzatic.tessera.lib.configio.unified.internal.DefaultTesseraProjectIO;

import java.util.List;

/**
 * Composition root for the standard {@link TesseraProjectIO} implementation.
 *
 * <p>Consumers depend on this factory and the {@code unified} contracts while the concrete
 * adapter remains an implementation detail.</p>
 */
public final class TesseraProjectIOFactory {

    private TesseraProjectIOFactory() {
    }

    /**
     * Creates a facade that discovers runtime extensions from the project itself.
     *
     * @return a default unified project facade
     */
    public static TesseraProjectIO createDefault() {
        return DefaultTesseraProjectIO.createDefault();
    }

    /**
     * Creates a facade with host-provided class loaders available to runtime discovery.
     *
     * @param preloadedClassLoaders class loaders supplied by the embedding application
     * @return a configured unified project facade
     * @throws NullPointerException when the list is null
     * @throws IllegalArgumentException when the list contains null
     */
    public static TesseraProjectIO createWithPreloadedClassLoaders(
            List<ClassLoader> preloadedClassLoaders
    ) {
        return DefaultTesseraProjectIO.createWithPreloadedClassLoaders(
                preloadedClassLoaders
        );
    }
}
