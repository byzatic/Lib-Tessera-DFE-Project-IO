package io.github.byzatic.tessera.lib.configio.infrastructure.loader;

import io.github.byzatic.tessera.lib.configio.application.service.ServiceEditorMetadataLoaderInterface;
import io.github.byzatic.tessera.lib.configio.domain.exception.PluginLoadingException;
import io.github.byzatic.tessera.lib.configio.domain.model.ServiceEditorMetadataDataObject;
import io.github.byzatic.tessera.lib.configio.infrastructure.factory.ServiceEditorMetadataLoaderFactory;
import io.github.byzatic.tessera.lib.configio.service_spi.ServiceParameterType;
import io.github.byzatic.tessera.lib.configio.service_spi.ServiceStorageRole;
import io.github.byzatic.tessera.lib.configio.support.TestProjectFixture;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class ServiceEditorMetadataLoaderStrategyTest {

    @Test
    public void shouldDiscoverServiceEditorMetadataFromServiceJar() throws Exception {
        try (TestProjectFixture fixture = TestProjectFixture.create();
             ServiceEditorMetadataLoaderInterface loader =
                     ServiceEditorMetadataLoaderFactory.create(
                             fixture.getProjectDirectory()
                                     .resolve("modules")
                                     .resolve("services"),
                             (ClassLoader) null
                     )) {
            List<ServiceEditorMetadataDataObject> available =
                    loader.getAvailableMetadata();

            assertEquals(1, available.size());
            ServiceEditorMetadataDataObject metadata = available.get(0);
            assertEquals("PrometheusExportService", metadata.getServiceId());
            assertEquals("Prometheus Export", metadata.getDescriptor().getDisplayName());
            assertEquals("2.4.0", metadata.getVersion());
            assertEquals("test-services.jar", metadata.getArtifactFileName());
            assertEquals(3, metadata.getDescriptor().getParameters().size());
            assertEquals(
                    ServiceStorageRole.INPUT,
                    metadata.getDescriptor().getParameters().get(0).getStorageRole()
            );
            assertEquals(
                    ServiceParameterType.SELECT,
                    metadata.getDescriptor().getParameters().get(1).getType()
            );
            assertEquals(
                    List.of("HTTP", "HTTPS"),
                    metadata.getDescriptor().getParameters().get(1).getSelectOptions()
            );
            assertTrue(loader.findMetadata("PrometheusExportService").isPresent());
            assertFalse(loader.findMetadata("UnknownService").isPresent());
        }
    }

    @Test
    public void shouldRejectAccessAfterClose() throws Exception {
        try (TestProjectFixture fixture = TestProjectFixture.create()) {
            ServiceEditorMetadataLoaderInterface loader =
                    ServiceEditorMetadataLoaderFactory.create(
                            fixture.getProjectDirectory()
                                    .resolve("modules")
                                    .resolve("services"),
                            (ClassLoader) null
                    );

            loader.close();

            assertThrows(PluginLoadingException.class, loader::getAvailableMetadata);
        }
    }

    @Test
    public void shouldRejectDuplicateServiceIds() throws Exception {
        try (TestProjectFixture fixture = TestProjectFixture.create()) {
            fixture.addDuplicateServiceMetadataJar();

            PluginLoadingException failure = assertThrows(
                    PluginLoadingException.class,
                    () -> ServiceEditorMetadataLoaderFactory.create(
                            fixture.getProjectDirectory()
                                    .resolve("modules")
                                    .resolve("services"),
                            (ClassLoader) null
                    )
            );

            assertTrue(failure.getMessage().contains("PrometheusExportService"));
        }
    }
}
