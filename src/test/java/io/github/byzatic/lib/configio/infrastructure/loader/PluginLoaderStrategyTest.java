package io.github.byzatic.lib.configio.infrastructure.loader;

import io.github.byzatic.lib.configio.application.module.ModuleLoaderInterface;
import io.github.byzatic.lib.configio.application.service.ServiceLoaderInterface;
import io.github.byzatic.lib.configio.infrastructure.factory.ModuleLoaderFactory;
import io.github.byzatic.lib.configio.infrastructure.factory.ServiceLoaderFactory;
import io.github.byzatic.lib.configio.support.TestProjectFixture;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PluginLoaderStrategyTest {

    @Test
    public void shouldDiscoverModulesAndServicesFromProjectJars() throws Exception {
        try (TestProjectFixture fixture = TestProjectFixture.create();
             ModuleLoaderInterface moduleLoader = ModuleLoaderFactory.create(
                     fixture.getProjectDirectory()
                             .resolve("modules")
                             .resolve("workflow_routines"),
                     (ClassLoader) null
             );
             ServiceLoaderInterface serviceLoader = ServiceLoaderFactory.create(
                     fixture.getProjectDirectory().resolve("modules").resolve("services"),
                     (ClassLoader) null
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
