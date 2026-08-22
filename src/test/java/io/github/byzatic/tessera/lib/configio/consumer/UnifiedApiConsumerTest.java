package io.github.byzatic.tessera.lib.configio.consumer;

import io.github.byzatic.tessera.lib.configio.unified.TesseraProjectIO;
import io.github.byzatic.tessera.lib.configio.unified.TesseraProjectIOFactory;
import io.github.byzatic.tessera.lib.configio.unified.spi.routine.BduiWidgetIds;
import io.github.byzatic.tessera.lib.configio.unified.spi.routine.RoutineEditorDescriptor;
import io.github.byzatic.tessera.lib.configio.unified.spi.routine.RoutineEditorDescriptorProvider;
import io.github.byzatic.tessera.lib.configio.unified.spi.routine.RoutineFunctionDescriptor;
import io.github.byzatic.tessera.lib.configio.unified.spi.service.ServiceEditorDescriptor;
import io.github.byzatic.tessera.lib.configio.unified.spi.service.ServiceEditorDescriptorProvider;
import io.github.byzatic.tessera.lib.configio.unified.spi.service.ServiceParameterDescriptor;
import io.github.byzatic.tessera.lib.configio.unified.spi.service.ServiceParameterType;
import io.github.byzatic.tessera.lib.configio.unified.spi.service.ServiceStorageRole;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

/** Verifies that an external consumer can use only contracts below the unified namespace. */
public class UnifiedApiConsumerTest {

    @Test
    public void shouldCreateProjectIoThroughPublicCompositionRoot() {
        TesseraProjectIO projectIO = TesseraProjectIOFactory.createDefault();

        assertNotNull(projectIO);
        assertThrows(
                IllegalArgumentException.class,
                () -> TesseraProjectIOFactory.createWithPreloadedClassLoaders(
                        Collections.singletonList(null)
                )
        );
    }

    @Test
    public void shouldPublishImmutableRoutineDescriptorThroughUnifiedSpi() {
        List<String> widgetIds = new ArrayList<String>();
        widgetIds.add(BduiWidgetIds.FUNC_ENV);
        RoutineFunctionDescriptor function = RoutineFunctionDescriptor.newBuilder()
                .functionId("Collect")
                .displayName("Collect")
                .bduiWidgetIds(widgetIds)
                .argumentIds(List.of("source"))
                .build();
        RoutineEditorDescriptorProvider provider = () -> RoutineEditorDescriptor.newBuilder()
                .routineId("CollectRoutine")
                .displayName("Collect routine")
                .functions(List.of(function))
                .build();

        widgetIds.add(BduiWidgetIds.FUNC_INPUT_DATA);

        assertEquals(
                List.of(BduiWidgetIds.FUNC_ENV),
                provider.getDescriptor().getFunctions().get(0).getBduiWidgetIds()
        );
    }

    @Test
    public void shouldPublishServiceDescriptorThroughUnifiedSpi() {
        ServiceParameterDescriptor parameter = ServiceParameterDescriptor.newBuilder()
                .parameterId("output")
                .displayName("Output storage")
                .type(ServiceParameterType.SELECT)
                .selectOptions(List.of("primary", "secondary"))
                .storageRole(ServiceStorageRole.OUTPUT)
                .build();
        ServiceEditorDescriptorProvider provider = () -> ServiceEditorDescriptor.newBuilder()
                .serviceId("ExportService")
                .displayName("Export service")
                .parameters(List.of(parameter))
                .build();

        assertEquals(
                ServiceStorageRole.OUTPUT,
                provider.getDescriptor().getParameters().get(0).getStorageRole()
        );
    }
}
