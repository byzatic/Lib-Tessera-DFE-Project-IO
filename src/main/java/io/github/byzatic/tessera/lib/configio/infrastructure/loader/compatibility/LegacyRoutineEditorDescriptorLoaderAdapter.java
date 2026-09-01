package io.github.byzatic.tessera.lib.configio.infrastructure.loader.compatibility;

import io.github.byzatic.tessera.lib.configio.infrastructure.utils.LegacyEditorDescriptorMapper;
import io.github.byzatic.tessera.lib.configio.routine_spi.RoutineEditorDescriptorProvider;
import io.github.byzatic.tessera.lib.configio.unified.spi.routine.RoutineEditorDescriptor;

import java.util.List;
import java.util.ServiceLoader;

/** Loads legacy routine SPI providers and adapts them to the unified contract. */
public final class LegacyRoutineEditorDescriptorLoaderAdapter {

    private final LegacyEditorDescriptorMapper descriptorMapper;

    public LegacyRoutineEditorDescriptorLoaderAdapter() {
        this.descriptorMapper = new LegacyEditorDescriptorMapper();
    }

    /** Returns immutable unified descriptors discovered through the legacy provider type. */
    public List<RoutineEditorDescriptor> load(ClassLoader classLoader) {
        return ServiceLoader.load(RoutineEditorDescriptorProvider.class, classLoader)
                .stream()
                .map(ServiceLoader.Provider::get)
                .map(RoutineEditorDescriptorProvider::getDescriptor)
                .map(descriptorMapper::mapRoutine)
                .toList();
    }
}
