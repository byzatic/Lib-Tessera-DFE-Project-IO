package io.github.byzatic.tessera.lib.configio.infrastructure.utils;

import io.github.byzatic.tessera.lib.configio.unified.spi.routine.RoutineEditorDescriptor;
import io.github.byzatic.tessera.lib.configio.unified.spi.routine.RoutineFunctionDescriptor;
import io.github.byzatic.tessera.lib.configio.unified.spi.service.ServiceEditorDescriptor;
import io.github.byzatic.tessera.lib.configio.unified.spi.service.ServiceParameterDescriptor;
import io.github.byzatic.tessera.lib.configio.unified.spi.service.ServiceParameterType;
import io.github.byzatic.tessera.lib.configio.unified.spi.service.ServiceStorageRole;

import java.util.Objects;

/** Maps compatibility SPI declarations to the unified editor contract. */
public final class LegacyEditorDescriptorMapper {

    /** Converts a legacy routine declaration to the unified SPI model. */
    public RoutineEditorDescriptor mapRoutine(
            io.github.byzatic.tessera.lib.configio.routine_spi.RoutineEditorDescriptor source
    ) {
        Objects.requireNonNull(source, "source");
        return RoutineEditorDescriptor.newBuilder()
                .routineId(source.getRoutineId())
                .displayName(source.getDisplayName())
                .description(source.getDescription())
                .functions(source.getFunctions().stream()
                        .map(this::mapRoutineFunction)
                        .toList())
                .build();
    }

    /** Converts a legacy service declaration to the unified SPI model. */
    public ServiceEditorDescriptor mapService(
            io.github.byzatic.tessera.lib.configio.service_spi.ServiceEditorDescriptor source
    ) {
        Objects.requireNonNull(source, "source");
        return ServiceEditorDescriptor.newBuilder()
                .serviceId(source.getServiceId())
                .displayName(source.getDisplayName())
                .description(source.getDescription())
                .parameters(source.getParameters().stream()
                        .map(this::mapServiceParameter)
                        .toList())
                .build();
    }

    private RoutineFunctionDescriptor mapRoutineFunction(
            io.github.byzatic.tessera.lib.configio.routine_spi.RoutineFunctionDescriptor source
    ) {
        return RoutineFunctionDescriptor.newBuilder()
                .functionId(source.getFunctionId())
                .displayName(source.getDisplayName())
                .description(source.getDescription())
                .bduiWidgetIds(source.getBduiWidgetIds())
                .argumentIds(source.getArgumentIds())
                .build();
    }

    private ServiceParameterDescriptor mapServiceParameter(
            io.github.byzatic.tessera.lib.configio.service_spi.ServiceParameterDescriptor source
    ) {
        return ServiceParameterDescriptor.newBuilder()
                .parameterId(source.getParameterId())
                .displayName(source.getDisplayName())
                .description(source.getDescription())
                .type(ServiceParameterType.valueOf(source.getType().name()))
                .defaultValue(source.getDefaultValue())
                .selectOptions(source.getSelectOptions())
                .storageRole(ServiceStorageRole.valueOf(source.getStorageRole().name()))
                .build();
    }
}
