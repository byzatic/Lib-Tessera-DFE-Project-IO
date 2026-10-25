package io.github.byzatic.tessera.lib.configio.unified;

import io.github.byzatic.tessera.lib.configio.support.TestProjectFixture;
import io.github.byzatic.tessera.lib.configio.unified.model.ExportProjectRequest;
import io.github.byzatic.tessera.lib.configio.unified.model.NodeId;
import io.github.byzatic.tessera.lib.configio.unified.model.SaveProjectRequest;
import io.github.byzatic.tessera.lib.configio.unified.model.SaveProjectResult;
import io.github.byzatic.tessera.lib.configio.unified.model.ServiceMetadata;
import io.github.byzatic.tessera.lib.configio.unified.model.RoutineMetadata;
import io.github.byzatic.tessera.lib.configio.unified.spi.routine.BduiWidgetIds;
import io.github.byzatic.tessera.lib.configio.unified.model.ConfigurationFileScope;
import io.github.byzatic.tessera.lib.configio.unified.model.ServiceParameterType;
import io.github.byzatic.tessera.lib.configio.unified.model.ServiceStorageRole;
import io.github.byzatic.tessera.lib.configio.unified.model.TesseraProject;
import org.junit.Test;
import io.github.byzatic.tessera.lib.configio.unified.model.ProjectArtifacts;
import java.util.List;
import java.util.ArrayList;
import java.util.jar.JarOutputStream;
import java.util.jar.JarEntry;
import java.util.zip.ZipFile;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class DefaultTesseraProjectIOTest {

    @Test
    public void shouldSaveAndExportSharedJars() throws Exception {
        TesseraProjectIO projectIO = TesseraProjectIOFactory.createDefault();
        Path temporaryDirectory = Files.createTempDirectory("shared-artifacts-test-");
        try (TestProjectFixture fixture = TestProjectFixture.create()) {
            Path sharedJar = temporaryDirectory.resolve("resources.jar");
            try (JarOutputStream jar = new JarOutputStream(Files.newOutputStream(sharedJar))) {
                jar.putNextEntry(new JarEntry("shared.txt"));
                jar.write(new byte[] {1, 2, 3});
                jar.closeEntry();
            }
            byte[] expected = Files.readAllBytes(sharedJar);
            TesseraProject project = projectIO.loadProject(fixture.getProjectDirectory());
            ProjectArtifacts artifacts = ProjectArtifacts.newBuilder().sharedJars(List.of(sharedJar)).build();
            Path savedDirectory = temporaryDirectory.resolve("saved");
            SaveProjectResult saved = projectIO.saveProject(SaveProjectRequest.newBuilder()
                    .projectDirectory(savedDirectory).project(project).artifacts(artifacts).build());
            assertArrayEquals(expected, Files.readAllBytes(savedDirectory.resolve("modules/shared/resources.jar")));
            Path exported = projectIO.exportProject(ExportProjectRequest.newBuilder()
                    .archiveDestination(temporaryDirectory.resolve("exported.zip"))
                    .project(project).artifacts(artifacts).build());
            for (Path archive : List.of(saved.getArchive(), exported)) {
                try (ZipFile zip = new ZipFile(archive.toFile())) {
                    var matches = zip.stream().filter(entry -> entry.getName().endsWith("modules/shared/resources.jar")).toList();
                    assertEquals(1, matches.size());
                    try (var input = zip.getInputStream(matches.get(0))) {
                        assertArrayEquals(expected, input.readAllBytes());
                    }
                }
            }
        } finally {
            deleteRecursively(temporaryDirectory);
        }
    }

    @Test
    public void shouldKeepSharedArtifactsImmutableAndIncludeThemInValueSemantics() {
        Path source = Path.of("artifacts", "..", "shared.jar");
        List<Path> paths = new ArrayList<>(List.of(source));
        ProjectArtifacts.Builder builder = ProjectArtifacts.newBuilder().sharedJars(paths);
        paths.clear();
        ProjectArtifacts artifacts = builder.build();
        assertEquals(List.of(source.toAbsolutePath().normalize()), artifacts.getSharedJars());
        assertThrows(UnsupportedOperationException.class, () -> artifacts.getSharedJars().clear());
        assertThrows(NullPointerException.class, () -> ProjectArtifacts.newBuilder().sharedJars(null));
        assertThrows(NullPointerException.class, () -> ProjectArtifacts.newBuilder().sharedJars(java.util.Arrays.asList((Path) null)));
        ProjectArtifacts equivalent = ProjectArtifacts.newBuilder().sharedJars(List.of(source)).build();
        assertEquals(equivalent, artifacts);
        assertEquals(equivalent.hashCode(), artifacts.hashCode());
        assertNotEquals(ProjectArtifacts.empty(), artifacts);
        assertTrue(ProjectArtifacts.empty().getSharedJars().isEmpty());
        assertTrue(artifacts.toString().contains("sharedJars="));
    }

    @Test
    public void shouldExposeCompleteProjectAsOneAggregate() throws Exception {
        TesseraProjectIO projectIO = TesseraProjectIOFactory.createDefault();
        try (TestProjectFixture fixture = TestProjectFixture.create()) {
            TesseraProject project = projectIO.loadProject(fixture.getProjectDirectory());

            assertEquals("test-project", project.getName());
            assertEquals(1, project.getNodes().size());
            assertEquals(1, project.getConfiguration().getStorages().size());
            assertEquals(1, project.getConfiguration().getServices().size());

            NodeId nodeId = project.getNodes().keySet().iterator().next();
            assertEquals(
                    3,
                    project.getNodes().get(nodeId).getConfiguration().getStorages().size()
            );
            assertEquals(3, project.getNodes().get(nodeId).getPipeline().getStages().size());
            assertTrue(project.getNodes().get(nodeId).getPipeline().getStages().stream()
                    .allMatch(stage -> stage.getWorkers().isEmpty()));
        }
    }

    @Test
    public void shouldSaveAndReloadThroughUnifiedApi() throws Exception {
        TesseraProjectIO projectIO = TesseraProjectIOFactory.createDefault();
        Path temporaryDirectory = Files.createTempDirectory("unified-project-io-test-");
        try (TestProjectFixture fixture = TestProjectFixture.create()) {
            TesseraProject source = projectIO.loadProject(fixture.getProjectDirectory());
            Path savedDirectory = temporaryDirectory.resolve("saved-project");

            SaveProjectResult result = projectIO.saveProject(
                    SaveProjectRequest.of(savedDirectory, source)
            );
            TesseraProject reloaded = projectIO.loadProject(savedDirectory);

            assertEquals(savedDirectory.toAbsolutePath(), result.getProjectDirectory());
            assertTrue(Files.isRegularFile(result.getArchive()));
            assertEquals(source, reloaded);

            Path exported = projectIO.exportProject(ExportProjectRequest.of(
                    temporaryDirectory.resolve("delivery"),
                    source
            ));
            assertEquals(temporaryDirectory.resolve("delivery.zip"), exported);
            assertTrue(Files.isRegularFile(exported));
        } finally {
            deleteRecursively(temporaryDirectory);
        }
    }

    @Test
    public void shouldOpenAndCloseRuntimeResourcesThroughOneSession() throws Exception {
        TesseraProjectIO projectIO = TesseraProjectIOFactory.createDefault();
        try (TestProjectFixture fixture = TestProjectFixture.create()) {
            ProjectRuntimeSession runtime = projectIO.openRuntime(fixture.getProjectDirectory());
            try {
                assertEquals("test-project", runtime.getProject().getName());
                assertEquals(4, runtime.getAvailableRoutineNames().size());
                assertEquals(1, runtime.getAvailableServiceNames().size());
                assertEquals(1, runtime.getServiceMetadata().size());
                RoutineMetadata routine = runtime.getRoutineMetadata().stream()
                        .filter(metadata -> metadata.getId().equals(
                                "GetDataWorkflowRoutine"
                        ))
                        .findFirst()
                        .orElseThrow();
                assertEquals(List.of(BduiWidgetIds.ROUTINE_CONFIGURATION_FILE),
                        routine.getRoutineWidgetIds());
                assertTrue(routine.getEnvironmentParameters().isEmpty());
                assertEquals("configurationFilePath",
                        routine.getConfigurationFileParameters().get(0).getKey());
                assertEquals("GeneratorConfigurationFile.json",
                        routine.getConfigurationFileParameters().get(0)
                                .getSuggestedFileName());
                assertEquals(List.of(".json"), routine.getConfigurationFileParameters()
                        .get(0).getAllowedExtensions());
                assertEquals(List.of(ConfigurationFileScope.NODE,
                                ConfigurationFileScope.PROJECT_GLOBAL),
                        routine.getConfigurationFileParameters().get(0).getAllowedScopes());
                assertEquals(ConfigurationFileScope.NODE,
                        routine.getConfigurationFileParameters().get(0).getDefaultScope());
                ServiceMetadata service = runtime.getServiceMetadata().get(0);
                assertEquals("PrometheusExportService", service.getId());
                assertEquals("Prometheus Export", service.getDisplayName());
                assertEquals("2.4.0", service.getVersion());
                assertEquals(3, service.getParameters().size());
                assertEquals(
                        ServiceStorageRole.INPUT,
                        service.getParameters().get(0).getStorageRole()
                );
                assertEquals(
                        ServiceParameterType.SELECT,
                        service.getParameters().get(1).getType()
                );
                assertEquals("HTTP", service.getParameters().get(1).getDefaultValue());
                assertEquals(
                        ServiceStorageRole.OUTPUT,
                        service.getParameters().get(2).getStorageRole()
                );
                assertFalse(runtime.isClosed());
            } finally {
                runtime.close();
            }
            assertTrue(runtime.isClosed());
        }
    }

    @Test
    public void shouldPublishUnifiedRevisionHandle() throws Exception {
        TesseraProjectIO projectIO = TesseraProjectIOFactory.createDefault();
        Path temporaryDirectory = Files.createTempDirectory("unified-revision-test-");
        AtomicReference<ProjectRevisionHandle> revision =
                new AtomicReference<ProjectRevisionHandle>();
        AtomicReference<ProjectRevisionError> error =
                new AtomicReference<ProjectRevisionError>();
        CountDownLatch published = new CountDownLatch(1);
        try (TestProjectFixture fixture = TestProjectFixture.create()) {
            TesseraProject project = projectIO.loadProject(fixture.getProjectDirectory());
            SaveProjectResult saved = projectIO.saveProject(SaveProjectRequest.of(
                    temporaryDirectory.resolve("watched-project"),
                    project
            ));

            ProjectRevisionSubscription subscription = projectIO.watchRevisions(
                    ProjectRevisionWatchRequest.builder(
                                    saved.getArchive(),
                                    temporaryDirectory.resolve("staging")
                            )
                            .pollInterval(Duration.ofMillis(10L))
                            .stableObservationCount(1)
                            .build(),
                    new ProjectRevisionListener() {
                        @Override
                        public void onRevisionAvailable(ProjectRevisionHandle available) {
                            revision.set(available);
                            published.countDown();
                        }

                        @Override
                        public void onRevisionRejected(ProjectRevisionError rejected) {
                            error.set(rejected);
                            published.countDown();
                        }
                    }
            );
            try {
                assertTrue(published.await(5L, TimeUnit.SECONDS));
                assertNull(error.get());
                assertEquals("test-project", revision.get().getProject().getName());
                revision.get().close();
                assertTrue(revision.get().isClosed());
            } finally {
                subscription.close();
                ProjectRevisionHandle unclosed = revision.get();
                if (unclosed != null && !unclosed.isClosed()) {
                    unclosed.close();
                }
            }
        } finally {
            deleteRecursively(temporaryDirectory);
        }
    }

    private void deleteRecursively(Path directory) throws IOException {
        if (!Files.exists(directory)) {
            return;
        }
        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attributes)
                    throws IOException {
                Files.deleteIfExists(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path visited, IOException failure)
                    throws IOException {
                if (failure != null) {
                    throw failure;
                }
                Files.deleteIfExists(visited);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
