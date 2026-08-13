package io.github.byzatic.lib.configio.infrastructure.loader;

import io.github.byzatic.lib.configio.application.loader.ProjectLoaderInterface;
import io.github.byzatic.lib.configio.application.module.ModuleLoaderInterface;
import io.github.byzatic.lib.configio.application.service.ServiceLoaderInterface;
import io.github.byzatic.lib.configio.domain.model.ProjectLoadResultDataObject;
import io.github.byzatic.lib.configio.infrastructure.factory.ModuleLoaderFactory;
import io.github.byzatic.lib.configio.infrastructure.factory.ProjectV1LoaderFactory;
import io.github.byzatic.lib.configio.infrastructure.factory.ServiceLoaderFactory;
import org.junit.Test;

import java.nio.file.Path;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PluginLoaderStrategyTest {

    @Test
    public void shouldDiscoverModulesAndServicesFromProjectJars() throws Exception {
        Path projectDirectory = Path.of(".develop", "MyAwsomeProject");
        ProjectLoaderInterface projectLoader = ProjectV1LoaderFactory.create();

        try (ProjectLoadResultDataObject project = projectLoader.load(projectDirectory);
             ModuleLoaderInterface moduleLoader = ModuleLoaderFactory.create(
                     projectDirectory.resolve("modules").resolve("workflow_routines"),
                     project.getSharedResourcesContainer()
             );
             ServiceLoaderInterface serviceLoader = ServiceLoaderFactory.create(
                     projectDirectory.resolve("modules").resolve("services"),
                     project.getSharedResourcesContainer()
             )) {
            Set<String> moduleNames = moduleLoader.getAvailableModuleNames();
            assertEquals(4, moduleNames.size());
            assertTrue(moduleNames.contains("GetDataWorkflowRoutine"));
            assertTrue(moduleNames.contains("ProcessingStatusWorkflowRoutine"));
            assertTrue(moduleNames.contains("DataEnrichmentWorkflowRoutine"));
            assertTrue(moduleNames.contains("GraphLiftingDataWorkflowRoutine"));

            Set<String> serviceNames = serviceLoader.getAvailableServiceNames();
            assertEquals(1, serviceNames.size());
            assertTrue(serviceNames.contains("PrometheusExportService"));
        }
    }
}
