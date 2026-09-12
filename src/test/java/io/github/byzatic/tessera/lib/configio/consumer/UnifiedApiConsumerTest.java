package io.github.byzatic.tessera.lib.configio.consumer;

import io.github.byzatic.tessera.lib.configio.unified.TesseraProjectIO;
import io.github.byzatic.tessera.lib.configio.unified.TesseraProjectIOFactory;
import io.github.byzatic.tessera.lib.configio.unified.spi.routine.BduiWidgetIds;
import io.github.byzatic.tessera.lib.configio.unified.spi.routine.ConfigurationFileScope;
import io.github.byzatic.tessera.lib.configio.unified.spi.routine.RoutineConfigurationFileDescriptor;
import io.github.byzatic.tessera.lib.configio.unified.spi.routine.RoutineEditorDescriptor;
import io.github.byzatic.tessera.lib.configio.unified.spi.routine.RoutineEditorDescriptorProvider;
import io.github.byzatic.tessera.lib.configio.unified.spi.routine.RoutineFunctionDescriptor;
import io.github.byzatic.tessera.lib.configio.unified.spi.routine.RoutineEnvironmentDescriptor;
import io.github.byzatic.tessera.lib.configio.unified.spi.service.ServiceEditorDescriptor;
import io.github.byzatic.tessera.lib.configio.unified.spi.service.ServiceEditorDescriptorProvider;
import io.github.byzatic.tessera.lib.configio.unified.spi.service.ServiceParameterDescriptor;
import io.github.byzatic.tessera.lib.configio.unified.spi.service.ServiceParameterType;
import io.github.byzatic.tessera.lib.configio.unified.spi.service.ServiceStorageRole;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.nio.file.Path;

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
                .routineWidgetIds(List.of(BduiWidgetIds.ROUTINE_ENV,
                        BduiWidgetIds.ROUTINE_CONFIGURATION_FILE))
                .environment(List.of(RoutineEnvironmentDescriptor.newBuilder()
                        .key("endpoint")
                        .displayName("Endpoint")
                        .required(true)
                        .build()))
                .configurationFiles(List.of(
                        RoutineConfigurationFileDescriptor.newBuilder()
                                .key("mapping")
                                .displayName("Mapping")
                                .suggestedFileName("mapping.json")
                                .allowedExtensions(List.of(".json"))
                                .allowedScopes(List.of(ConfigurationFileScope.NODE))
                                .defaultScope(ConfigurationFileScope.NODE)
                                .build()
                ))
                .functions(List.of(function))
                .build();

        widgetIds.add(BduiWidgetIds.FUNC_INPUT_DATA);

        assertEquals(
                List.of(BduiWidgetIds.FUNC_ENV),
                provider.getDescriptor().getFunctions().get(0).getBduiWidgetIds()
        );
        assertEquals(List.of(BduiWidgetIds.ROUTINE_ENV,
                        BduiWidgetIds.ROUTINE_CONFIGURATION_FILE),
                provider.getDescriptor().getRoutineWidgetIds());
        assertEquals("endpoint", provider.getDescriptor().getEnvironment()
                .get(0).getKey());
        assertEquals(ConfigurationFileScope.NODE,
                provider.getDescriptor().getConfigurationFiles().get(0).getDefaultScope());
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

    @Test
    public void shouldRejectInvalidRoutineEditorMetadata() {
        RoutineEnvironmentDescriptor environment = RoutineEnvironmentDescriptor.newBuilder()
                .key("configurationFilePath")
                .displayName("Configuration path")
                .build();
        RoutineConfigurationFileDescriptor file = validConfigurationFile();

        assertThrows(IllegalArgumentException.class, () -> routineBuilder()
                .routineWidgetIds(List.of(BduiWidgetIds.ROUTINE_ENV,
                        BduiWidgetIds.ROUTINE_CONFIGURATION_FILE))
                .environment(List.of(environment))
                .configurationFiles(List.of(file))
                .build());
        assertThrows(IllegalArgumentException.class, () -> routineBuilder()
                .routineWidgetIds(List.of(BduiWidgetIds.ROUTINE_CONFIGURATION_FILE))
                .build());
        assertThrows(IllegalArgumentException.class, () -> routineBuilder()
                .routineWidgetIds(List.of("UnknownWidget"))
                .build());
        assertThrows(IllegalArgumentException.class,
                () -> RoutineConfigurationFileDescriptor.newBuilder()
                        .key("configurationFilePath")
                        .displayName("Configuration")
                        .suggestedFileName("../configuration.json")
                        .allowedExtensions(List.of(".json"))
                        .allowedScopes(List.of(ConfigurationFileScope.NODE))
                        .defaultScope(ConfigurationFileScope.NODE)
                        .build());
        assertThrows(IllegalArgumentException.class,
                () -> RoutineConfigurationFileDescriptor.newBuilder()
                        .key("configurationFilePath")
                        .displayName("Configuration")
                        .suggestedFileName("configuration.json")
                        .allowedScopes(List.of(ConfigurationFileScope.NODE))
                        .defaultScope(ConfigurationFileScope.PROJECT_GLOBAL)
                        .build());
    }

    @Test
    public void shouldSupportFreeRoutineEnvironmentAndProtectDetachedMetadata() {
        RoutineEditorDescriptor freeEnvironment = routineBuilder()
                .routineWidgetIds(List.of(BduiWidgetIds.ROUTINE_ENV))
                .allowCustomEnvironmentKeys(true)
                .build();

        assertEquals(true, freeEnvironment.isAllowCustomEnvironmentKeys());
        assertEquals(List.of(), freeEnvironment.getEnvironment());
        assertThrows(IllegalArgumentException.class,
                () -> io.github.byzatic.tessera.lib.configio.unified.model
                        .RoutineConfigurationFileParameter.newBuilder()
                        .key("configurationFilePath")
                        .displayName("Configuration")
                        .suggestedFileName("../configuration.json")
                        .allowedScopes(List.of())
                        .defaultScope(io.github.byzatic.tessera.lib.configio.unified.model
                                .ConfigurationFileScope.NODE)
                        .build());
        assertThrows(IllegalArgumentException.class,
                () -> io.github.byzatic.tessera.lib.configio.unified.model.RoutineMetadata
                        .newBuilder()
                        .id("TestRoutine")
                        .displayName("Test routine")
                        .description("")
                        .version("1.0.0")
                        .artifact(Path.of("test.jar"))
                        .routineWidgetIds(List.of(BduiWidgetIds.ROUTINE_ENV))
                        .build());
    }

    private RoutineEditorDescriptor.Builder routineBuilder() {
        return RoutineEditorDescriptor.newBuilder()
                .routineId("TestRoutine")
                .displayName("Test routine");
    }

    private RoutineConfigurationFileDescriptor validConfigurationFile() {
        return RoutineConfigurationFileDescriptor.newBuilder()
                .key("configurationFilePath")
                .displayName("Configuration")
                .suggestedFileName("configuration.json")
                .allowedExtensions(List.of(".json"))
                .allowedScopes(List.of(ConfigurationFileScope.NODE,
                        ConfigurationFileScope.PROJECT_GLOBAL))
                .defaultScope(ConfigurationFileScope.NODE)
                .build();
    }
}
