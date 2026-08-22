package io.github.byzatic.tessera.lib.configio.support;

import io.github.byzatic.tessera.lib.configio.routine_spi.BduiWidgetIds;
import io.github.byzatic.tessera.lib.configio.routine_spi.RoutineEditorDescriptor;
import io.github.byzatic.tessera.lib.configio.routine_spi.RoutineEditorDescriptorProvider;
import io.github.byzatic.tessera.lib.configio.routine_spi.RoutineFunctionDescriptor;
import io.github.byzatic.tessera.lib.configio.service_spi.ServiceEditorDescriptor;
import io.github.byzatic.tessera.lib.configio.service_spi.ServiceEditorDescriptorProvider;
import io.github.byzatic.tessera.lib.configio.service_spi.ServiceParameterDescriptor;
import io.github.byzatic.tessera.lib.configio.service_spi.ServiceParameterType;
import io.github.byzatic.tessera.lib.configio.service_spi.ServiceStorageRole;
import io.github.byzatic.tessera.service.api_engine.MCg3ServiceApiInterface;
import io.github.byzatic.tessera.service.service.ServiceFactoryInterface;
import io.github.byzatic.tessera.service.service.ServiceInterface;
import io.github.byzatic.tessera.service.service.health.HealthFlagProxy;
import io.github.byzatic.tessera.workflowroutine.api_engine.MCg3WorkflowRoutineApiInterface;
import io.github.byzatic.tessera.workflowroutine.workflowroutines.WorkflowRoutineFactoryInterface;
import io.github.byzatic.tessera.workflowroutine.workflowroutines.WorkflowRoutineInterface;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public final class TestProjectFixture implements AutoCloseable {

    private static final String MODULE_FACTORY_INTERFACE =
            "io.github.byzatic.tessera.workflowroutine.workflowroutines."
                    + "WorkflowRoutineFactoryInterface";
    private static final String SERVICE_FACTORY_INTERFACE =
            "io.github.byzatic.tessera.service.service.ServiceFactoryInterface";
    private static final String ROUTINE_EDITOR_DESCRIPTOR_PROVIDER_INTERFACE =
            "io.github.byzatic.tessera.lib.configio.routine_spi."
                    + "RoutineEditorDescriptorProvider";
    private static final String SERVICE_EDITOR_DESCRIPTOR_PROVIDER_INTERFACE =
            "io.github.byzatic.tessera.lib.configio.service_spi."
                    + "ServiceEditorDescriptorProvider";

    private final Path projectDirectory;
    private final Path moduleJar;
    private final Path serviceJar;

    private TestProjectFixture(Path projectDirectory, Path moduleJar, Path serviceJar) {
        this.projectDirectory = projectDirectory;
        this.moduleJar = moduleJar;
        this.serviceJar = serviceJar;
    }

    public static TestProjectFixture create() throws IOException {
        Path projectDirectory = Files.createTempDirectory("config-io-test-project-");
        Path dataDirectory = Files.createDirectories(projectDirectory.resolve("data"));
        Path nodeDirectory = Files.createDirectories(
                dataDirectory.resolve("nodes").resolve("root-test-node")
        );
        Path sharedDirectory = Files.createDirectories(
                projectDirectory.resolve("modules").resolve("shared")
        );
        Path modulesDirectory = Files.createDirectories(
                projectDirectory.resolve("modules").resolve("workflow_routines")
        );
        Path servicesDirectory = Files.createDirectories(
                projectDirectory.resolve("modules").resolve("services")
        );

        Files.writeString(dataDirectory.resolve("Project.json"), projectJson());
        Files.writeString(dataDirectory.resolve("Global.json"), globalJson());
        Files.writeString(nodeDirectory.resolve("global.json"), nodeGlobalJson());
        Files.writeString(nodeDirectory.resolve("pipeline.json"), pipelineJson());

        Path moduleJar = modulesDirectory.resolve("test-workflow-routines.jar");
        createModuleProviderJar(
                moduleJar,
                new Class<?>[]{
                        GetDataWorkflowRoutineFactory.class,
                        ProcessingStatusWorkflowRoutineFactory.class,
                        DataEnrichmentWorkflowRoutineFactory.class,
                        GraphLiftingDataWorkflowRoutineFactory.class
                },
                DataEnrichmentEditorDescriptorProvider.class
        );
        Path serviceJar = servicesDirectory.resolve("test-services.jar");
        createServiceProviderJar(
                serviceJar,
                new Class<?>[]{PrometheusExportServiceFactory.class},
                PrometheusExportEditorDescriptorProvider.class
        );

        // The directory is intentionally empty; its existence is part of the project format.
        if (!Files.isDirectory(sharedDirectory)) {
            throw new IOException("Cannot create shared resources directory");
        }
        return new TestProjectFixture(projectDirectory, moduleJar, serviceJar);
    }

    public Path getProjectDirectory() {
        return projectDirectory;
    }

    public Path getModuleJar() {
        return moduleJar;
    }

    public Path getServiceJar() {
        return serviceJar;
    }

    public Path addDuplicateRoutineMetadataJar() throws IOException {
        Path duplicateJar = moduleJar.getParent().resolve("duplicate-routine-metadata.jar");
        createMetadataProviderJar(
                duplicateJar,
                DuplicateDataEnrichmentEditorDescriptorProvider.class
        );
        return duplicateJar;
    }

    public Path addDuplicateServiceMetadataJar() throws IOException {
        Path duplicateJar = serviceJar.getParent().resolve("duplicate-service-metadata.jar");
        createServiceMetadataProviderJar(
                duplicateJar,
                DuplicatePrometheusExportEditorDescriptorProvider.class
        );
        return duplicateJar;
    }

    @Override
    public void close() throws IOException {
        Files.walkFileTree(projectDirectory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attributes)
                    throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path directory, IOException exception)
                    throws IOException {
                if (exception != null) {
                    throw exception;
                }
                Files.delete(directory);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static void createServiceProviderJar(
            Path jarFile,
            Class<?>[] serviceFactories,
            Class<?>... metadataProviders
    ) throws IOException {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().put(Attributes.Name.IMPLEMENTATION_VERSION, "2.4.0");
        try (JarOutputStream output = new JarOutputStream(
                Files.newOutputStream(jarFile),
                manifest
        )) {
            writeServiceEntry(output, SERVICE_FACTORY_INTERFACE, serviceFactories);
            writeServiceEntry(
                    output,
                    SERVICE_EDITOR_DESCRIPTOR_PROVIDER_INTERFACE,
                    metadataProviders
            );
        }
    }

    private static void createServiceMetadataProviderJar(
            Path jarFile,
            Class<?>... metadataProviders
    ) throws IOException {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        try (JarOutputStream output = new JarOutputStream(
                Files.newOutputStream(jarFile),
                manifest
        )) {
            writeServiceEntry(
                    output,
                    SERVICE_EDITOR_DESCRIPTOR_PROVIDER_INTERFACE,
                    metadataProviders
            );
        }
    }

    private static void createModuleProviderJar(
            Path jarFile,
            Class<?>[] moduleFactories,
            Class<?>... metadataProviders
    ) throws IOException {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().put(Attributes.Name.IMPLEMENTATION_VERSION, "1.2.3");
        try (JarOutputStream output = new JarOutputStream(
                Files.newOutputStream(jarFile),
                manifest
        )) {
            writeServiceEntry(output, MODULE_FACTORY_INTERFACE, moduleFactories);
            writeServiceEntry(
                    output,
                    ROUTINE_EDITOR_DESCRIPTOR_PROVIDER_INTERFACE,
                    metadataProviders
            );
        }
    }

    private static void createMetadataProviderJar(
            Path jarFile,
            Class<?>... metadataProviders
    ) throws IOException {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        try (JarOutputStream output = new JarOutputStream(
                Files.newOutputStream(jarFile),
                manifest
        )) {
            writeServiceEntry(
                    output,
                    ROUTINE_EDITOR_DESCRIPTOR_PROVIDER_INTERFACE,
                    metadataProviders
            );
        }
    }

    private static void writeServiceEntry(
            JarOutputStream output,
            String serviceInterface,
            Class<?>... providerClasses
    ) throws IOException {
        StringBuilder providers = new StringBuilder();
        for (Class<?> providerClass : providerClasses) {
            providers.append(providerClass.getName()).append('\n');
        }
        output.putNextEntry(new JarEntry("META-INF/services/" + serviceInterface));
        output.write(providers.toString().getBytes(StandardCharsets.UTF_8));
        output.closeEntry();
    }

    private static String projectJson() {
        return """
                {
                  "project_config_version": "v1.0.0-SingleRootStrictNestedNodeTree",
                  "project_name": "test-project",
                  "structure": {
                    "id": "root",
                    "name": "test-node",
                    "description": "Test node",
                    "downstream": []
                  }
                }
                """;
    }

    private static String globalJson() {
        return """
                {
                  "storages": [
                    {"id_name": "GLOBAL_STORAGE", "description": "Global", "options": []}
                  ],
                  "services": [
                    {"id_name": "TestService", "description": "Service", "options": []}
                  ]
                }
                """;
    }

    private static String nodeGlobalJson() {
        return """
                {
                  "storages": [
                    {"id_name": "FIRST", "description": "First", "options": []},
                    {"id_name": "SECOND", "description": "Second", "options": []},
                    {"id_name": "THIRD", "description": "Third", "options": []}
                  ]
                }
                """;
    }

    private static String pipelineJson() {
        return """
                {
                  "stages_consistency": [
                    {"position": 1, "stage_id": "GetData"},
                    {"position": 2, "stage_id": "Process"},
                    {"position": 3, "stage_id": "Save"}
                  ],
                  "stages_description": []
                }
                """;
    }

    public static final class GetDataWorkflowRoutineFactory
            implements WorkflowRoutineFactoryInterface {

        @Override
        public WorkflowRoutineInterface create(
                MCg3WorkflowRoutineApiInterface workflowRoutineApi,
                io.github.byzatic.tessera.workflowroutine.workflowroutines.health.HealthFlagProxy
                        healthFlagProxy
        ) {
            return null;
        }
    }

    public static final class ProcessingStatusWorkflowRoutineFactory
            implements WorkflowRoutineFactoryInterface {

        @Override
        public WorkflowRoutineInterface create(
                MCg3WorkflowRoutineApiInterface workflowRoutineApi,
                io.github.byzatic.tessera.workflowroutine.workflowroutines.health.HealthFlagProxy
                        healthFlagProxy
        ) {
            return null;
        }
    }

    public static final class DataEnrichmentWorkflowRoutineFactory
            implements WorkflowRoutineFactoryInterface {

        @Override
        public WorkflowRoutineInterface create(
                MCg3WorkflowRoutineApiInterface workflowRoutineApi,
                io.github.byzatic.tessera.workflowroutine.workflowroutines.health.HealthFlagProxy
                        healthFlagProxy
        ) {
            return null;
        }
    }

    public static final class GraphLiftingDataWorkflowRoutineFactory
            implements WorkflowRoutineFactoryInterface {

        @Override
        public WorkflowRoutineInterface create(
                MCg3WorkflowRoutineApiInterface workflowRoutineApi,
                io.github.byzatic.tessera.workflowroutine.workflowroutines.health.HealthFlagProxy
                        healthFlagProxy
        ) {
            return null;
        }
    }

    public static final class DataEnrichmentEditorDescriptorProvider
            implements RoutineEditorDescriptorProvider {

        @Override
        public RoutineEditorDescriptor getDescriptor() {
            RoutineFunctionDescriptor function = RoutineFunctionDescriptor.newBuilder()
                    .functionId("AddLabel")
                    .displayName("Add Label")
                    .description("Adds or replaces labels on a metric.")
                    .bduiWidgetIds(List.of(
                            BduiWidgetIds.FUNC_ENV,
                            BduiWidgetIds.FUNC_INPUT_DATA,
                            BduiWidgetIds.FUNC_OUTPUT_DATA
                    ))
                    .argumentIds(List.of("DataId"))
                    .build();
            return RoutineEditorDescriptor.newBuilder()
                    .routineId("DataEnrichmentWorkflowRoutine")
                    .displayName("Data Enrichment")
                    .description("Enriches metric data with labels and graph context.")
                    .functions(List.of(function))
                    .build();
        }
    }

    public static final class DuplicateDataEnrichmentEditorDescriptorProvider
            implements RoutineEditorDescriptorProvider {

        @Override
        public RoutineEditorDescriptor getDescriptor() {
            return RoutineEditorDescriptor.newBuilder()
                    .routineId("DataEnrichmentWorkflowRoutine")
                    .displayName("Duplicate Data Enrichment")
                    .functions(List.of())
                    .build();
        }
    }

    public static final class PrometheusExportServiceFactory
            implements ServiceFactoryInterface {

        @Override
        public ServiceInterface create(
                MCg3ServiceApiInterface serviceApi,
                HealthFlagProxy healthFlagProxy
        ) {
            return null;
        }
    }

    public static final class PrometheusExportEditorDescriptorProvider
            implements ServiceEditorDescriptorProvider {

        @Override
        public ServiceEditorDescriptor getDescriptor() {
            ServiceParameterDescriptor inputStorage =
                    ServiceParameterDescriptor.newBuilder()
                            .parameterId("inputStorage")
                            .displayName("Input storage")
                            .description("Storage containing metrics to export.")
                            .storageRole(ServiceStorageRole.INPUT)
                            .build();
            ServiceParameterDescriptor protocol =
                    ServiceParameterDescriptor.newBuilder()
                            .parameterId("protocol")
                            .displayName("Protocol")
                            .type(ServiceParameterType.SELECT)
                            .defaultValue("HTTP")
                            .selectOptions(List.of("HTTP", "HTTPS"))
                            .build();
            ServiceParameterDescriptor outputStorage =
                    ServiceParameterDescriptor.newBuilder()
                            .parameterId("outputStorage")
                            .displayName("Output storage")
                            .storageRole(ServiceStorageRole.OUTPUT)
                            .build();
            return ServiceEditorDescriptor.newBuilder()
                    .serviceId("PrometheusExportService")
                    .displayName("Prometheus Export")
                    .description("Exports metrics in Prometheus format.")
                    .parameters(List.of(inputStorage, protocol, outputStorage))
                    .build();
        }
    }

    public static final class DuplicatePrometheusExportEditorDescriptorProvider
            implements ServiceEditorDescriptorProvider {

        @Override
        public ServiceEditorDescriptor getDescriptor() {
            return ServiceEditorDescriptor.newBuilder()
                    .serviceId("PrometheusExportService")
                    .displayName("Duplicate Prometheus Export")
                    .build();
        }
    }
}
