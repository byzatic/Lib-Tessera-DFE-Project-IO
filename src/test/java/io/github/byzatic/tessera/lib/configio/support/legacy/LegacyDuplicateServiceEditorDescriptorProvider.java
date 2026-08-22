package io.github.byzatic.tessera.lib.configio.support.legacy;

import io.github.byzatic.tessera.lib.configio.service_spi.ServiceEditorDescriptor;
import io.github.byzatic.tessera.lib.configio.service_spi.ServiceEditorDescriptorProvider;

/** Legacy provider used to verify compatibility and cross-SPI duplicate detection. */
public final class LegacyDuplicateServiceEditorDescriptorProvider
        implements ServiceEditorDescriptorProvider {

    @Override
    public ServiceEditorDescriptor getDescriptor() {
        return ServiceEditorDescriptor.newBuilder()
                .serviceId("PrometheusExportService")
                .displayName("Duplicate Prometheus Export")
                .build();
    }
}
