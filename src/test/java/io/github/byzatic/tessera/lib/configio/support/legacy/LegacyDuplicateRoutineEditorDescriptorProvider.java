package io.github.byzatic.tessera.lib.configio.support.legacy;

import io.github.byzatic.tessera.lib.configio.routine_spi.RoutineEditorDescriptor;
import io.github.byzatic.tessera.lib.configio.routine_spi.RoutineEditorDescriptorProvider;

import java.util.List;

/** Legacy provider used to verify compatibility and cross-SPI duplicate detection. */
public final class LegacyDuplicateRoutineEditorDescriptorProvider
        implements RoutineEditorDescriptorProvider {

    @Override
    public RoutineEditorDescriptor getDescriptor() {
        return RoutineEditorDescriptor.newBuilder()
                .routineId("GetDataWorkflowRoutine")
                .displayName("Duplicate Get Data")
                .functions(List.of())
                .build();
    }
}
