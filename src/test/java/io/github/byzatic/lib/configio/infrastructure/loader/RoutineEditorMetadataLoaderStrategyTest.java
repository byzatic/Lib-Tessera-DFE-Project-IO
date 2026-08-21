package io.github.byzatic.lib.configio.infrastructure.loader;

import io.github.byzatic.lib.configio.application.module.RoutineEditorMetadataLoaderInterface;
import io.github.byzatic.lib.configio.domain.exception.PluginLoadingException;
import io.github.byzatic.lib.configio.domain.model.RoutineEditorMetadataDataObject;
import io.github.byzatic.lib.configio.infrastructure.factory.RoutineEditorMetadataLoaderFactory;
import io.github.byzatic.lib.configio.support.TestProjectFixture;
import org.junit.Test;

import java.util.List;

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
            assertEquals("DataEnrichmentWorkflowRoutine", metadata.getRoutineId());
            assertEquals("Data Enrichment", metadata.getDescriptor().getDisplayName());
            assertEquals("1.2.3", metadata.getVersion());
            assertEquals("test-workflow-routines.jar", metadata.getArtifactFileName());
            assertEquals(
                    List.of("FuncENV", "FuncInputData", "FuncOutputData"),
                    metadata.getDescriptor().getFunctions().get(0).getBduiWidgetIds()
            );
            assertEquals(
                    List.of("DataId"),
                    metadata.getDescriptor().getFunctions().get(0).getArgumentIds()
            );
            assertTrue(loader.findMetadata("DataEnrichmentWorkflowRoutine").isPresent());
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

            assertTrue(failure.getMessage().contains("DataEnrichmentWorkflowRoutine"));
        }
    }
}
