package io.github.byzatic.tessera.lib.configio.infrastructure.loader;

import io.github.byzatic.tessera.lib.configio.application.module.RoutineEditorMetadataLoaderInterface;
import io.github.byzatic.tessera.lib.configio.domain.exception.PluginLoadingException;
import io.github.byzatic.tessera.lib.configio.domain.model.RoutineEditorMetadataDataObject;
import io.github.byzatic.tessera.lib.configio.infrastructure.factory.RoutineEditorMetadataLoaderFactory;
import io.github.byzatic.tessera.lib.configio.support.TestProjectFixture;
import org.junit.Test;

import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class RoutineEditorMetadataLoaderStrategyTest {

    @Test
    public void shouldDiscoverRoutineEditorMetadataFromModuleJar() throws Exception {
        try (TestProjectFixture fixture = TestProjectFixture.create();
             RoutineEditorMetadataLoaderInterface loader =
                     RoutineEditorMetadataLoaderFactory.create(
                             fixture.getProjectDirectory()
                                     .resolve("modules")
                                     .resolve("workflow_routines"),
                             (ClassLoader) null
                     )) {
            List<RoutineEditorMetadataDataObject> available =
                    loader.getAvailableMetadata();

            assertEquals(1, available.size());
            RoutineEditorMetadataDataObject metadata = available.get(0);
            assertEquals("GetDataWorkflowRoutine", metadata.getRoutineId());
            assertEquals("Get Data", metadata.getDescriptor().getDisplayName());
            assertEquals("1.2.3", metadata.getVersion());
            assertEquals("test-workflow-routines.jar", metadata.getArtifactFileName());
            assertEquals(
                    List.of("FuncENV", "FuncInputData", "FuncOutputData"),
                    metadata.getDescriptor().getFunctions().get(0).getBduiWidgetIds()
            );
            assertEquals(
                    List.of(),
                    metadata.getDescriptor().getFunctions().get(0).getArgumentIds()
            );
            assertTrue(loader.findMetadata("GetDataWorkflowRoutine").isPresent());
            assertFalse(loader.findMetadata("UnknownRoutine").isPresent());
        }
    }

    @Test
    public void shouldRejectAccessAfterClose() throws Exception {
        try (TestProjectFixture fixture = TestProjectFixture.create()) {
            RoutineEditorMetadataLoaderInterface loader =
                    RoutineEditorMetadataLoaderFactory.create(
                            fixture.getProjectDirectory()
                                    .resolve("modules")
                                    .resolve("workflow_routines"),
                            (ClassLoader) null
                    );

            loader.close();

            assertThrows(PluginLoadingException.class, loader::getAvailableMetadata);
        }
    }

    @Test
    public void shouldRejectDuplicateRoutineIds() throws Exception {
        try (TestProjectFixture fixture = TestProjectFixture.create()) {
            fixture.addDuplicateRoutineMetadataJar();

            PluginLoadingException failure = assertThrows(
                    PluginLoadingException.class,
                    () -> RoutineEditorMetadataLoaderFactory.create(
                            fixture.getProjectDirectory()
                                    .resolve("modules")
                                    .resolve("workflow_routines"),
                            (ClassLoader) null
                    )
            );

            assertTrue(failure.getMessage().contains("GetDataWorkflowRoutine"));
        }
    }

    @Test
    public void shouldLoadProviderCompiledAgainstPreviousUnifiedApi() throws Exception {
        Path directory = Files.createTempDirectory("legacy-unified-routine-");
        Path jar = directory.resolve("legacy-unified-routine-provider.jar");
        try (var input = getClass().getResourceAsStream(
                "/legacy-unified-routine-provider.jar"
        )) {
            Files.copy(java.util.Objects.requireNonNull(input), jar);
        }
        try (RoutineEditorMetadataLoaderInterface loader =
                     RoutineEditorMetadataLoaderFactory.create(directory, (ClassLoader) null)) {
            RoutineEditorMetadataDataObject metadata = loader.getAvailableMetadata().get(0);

            assertEquals("LegacyUnifiedRoutine", metadata.getRoutineId());
            assertEquals(List.of(), metadata.getDescriptor().getRoutineWidgetIds());
            assertEquals(List.of(), metadata.getDescriptor().getEnvironment());
            assertEquals(List.of(), metadata.getDescriptor().getConfigurationFiles());
            assertEquals(List.of("FuncENV"), metadata.getDescriptor().getFunctions()
                    .get(0).getBduiWidgetIds());
        } finally {
            Files.deleteIfExists(jar);
            Files.deleteIfExists(directory);
        }
    }
}
