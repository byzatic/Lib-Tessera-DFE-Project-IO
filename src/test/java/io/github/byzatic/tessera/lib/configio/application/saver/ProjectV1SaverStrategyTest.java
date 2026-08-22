package io.github.byzatic.tessera.lib.configio.application.saver;

import io.github.byzatic.tessera.lib.configio.application.loader.ProjectLoaderInterface;
import io.github.byzatic.tessera.lib.configio.domain.model.GraphNodeReferenceDataObject;
import io.github.byzatic.tessera.lib.configio.domain.model.NodeContainerDataObject;
import io.github.byzatic.tessera.lib.configio.domain.model.NodeDataObject;
import io.github.byzatic.tessera.lib.configio.domain.model.ProjectLoadResultDataObject;
import io.github.byzatic.tessera.lib.configio.infrastructure.factory.ProjectV1LoaderFactory;
import io.github.byzatic.tessera.lib.configio.infrastructure.factory.ProjectV1SaverFactory;
import io.github.byzatic.tessera.lib.configio.infrastructure.dto.raw.global.Global;
import io.github.byzatic.tessera.lib.configio.infrastructure.dto.raw.node.global.NodeGlobal;
import io.github.byzatic.tessera.lib.configio.infrastructure.dto.raw.node.pipeline.Pipeline;
import io.github.byzatic.tessera.lib.configio.infrastructure.dto.raw.project_and_graph.Project;
import io.github.byzatic.tessera.lib.configio.infrastructure.strategy.NodePathResolverStrategy;
import io.github.byzatic.tessera.lib.configio.infrastructure.util.GsonJsonFileReaderUtility;
import io.github.byzatic.tessera.lib.configio.support.TestProjectFixture;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ProjectV1SaverStrategyTest {

    @Test
    public void shouldSaveProjectThatCanBeLoadedAgain() throws Exception {
        ProjectLoaderInterface loader = ProjectV1LoaderFactory.create();
        ProjectSaverInterface saver = ProjectV1SaverFactory.create();
        Path testDirectory = Files.createTempDirectory("project-v1-saver-test-");
        Path targetDirectory = testDirectory.resolve("saved-project");

        try (TestProjectFixture fixture = TestProjectFixture.create()) {
            Path sourceDirectory = fixture.getProjectDirectory();
            try (ProjectLoadResultDataObject source = loader.load(sourceDirectory)) {
                Path archive = saver.save(
                        targetDirectory,
                        source.getGlobal(),
                        source.getNodeContainer()
                );
                assertEquals(testDirectory.resolve("saved-project.zip"), archive);
                assertTrue(Files.isRegularFile(archive));
                assertArchiveContainsProjectFiles(archive, "saved-project");

                try (ProjectLoadResultDataObject saved = loader.load(targetDirectory)) {
                    assertEquivalent(source, saved);
                }
            }
        } finally {
            deleteTree(testDirectory);
        }
    }

    @Test
    public void shouldSaveModuleAndServiceJarsBeforeCreatingArchive() throws Exception {
        ProjectLoaderInterface loader = ProjectV1LoaderFactory.create();
        ProjectSaverInterface saver = ProjectV1SaverFactory.create();
        Path testDirectory = Files.createTempDirectory("project-v1-plugin-saver-test-");
        Path targetDirectory = testDirectory.resolve("assembled-project");

        try (TestProjectFixture fixture = TestProjectFixture.create()) {
            Path sourceDirectory = fixture.getProjectDirectory();
            Path moduleJar = fixture.getModuleJar();
            Path serviceJar = fixture.getServiceJar();
            try (ProjectLoadResultDataObject source = loader.load(sourceDirectory)) {
                Path archive = saver.save(
                        targetDirectory,
                        source.getGlobal(),
                        source.getNodeContainer(),
                        Arrays.asList(moduleJar),
                        Arrays.asList(serviceJar)
                );

                Path savedModule = targetDirectory
                        .resolve("modules")
                        .resolve("workflow_routines")
                        .resolve(moduleJar.getFileName());
                Path savedService = targetDirectory
                        .resolve("modules")
                        .resolve("services")
                        .resolve(serviceJar.getFileName());
                assertTrue(Files.isRegularFile(savedModule));
                assertTrue(Files.isRegularFile(savedService));
                assertEquals(Files.size(moduleJar), Files.size(savedModule));
                assertEquals(Files.size(serviceJar), Files.size(savedService));
                assertArchiveContains(
                        archive,
                        "assembled-project/modules/workflow_routines/"
                                + moduleJar.getFileName(),
                        "assembled-project/modules/services/" + serviceJar.getFileName()
                );
            }
        } finally {
            deleteTree(testDirectory);
        }
    }

    private void assertArchiveContainsProjectFiles(Path archive, String projectName)
            throws IOException {
        assertArchiveContains(
                archive,
                projectName + "/",
                projectName + "/data/Project.json",
                projectName + "/data/Global.json",
                projectName + "/modules/shared/",
                projectName + "/modules/workflow_routines/",
                projectName + "/modules/services/"
        );
    }

    private void assertArchiveContains(Path archive, String... expectedEntries)
            throws IOException {
        Set<String> entryNames = new HashSet<String>();
        try (ZipFile zipFile = new ZipFile(archive.toFile())) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                entryNames.add(entries.nextElement().getName());
            }
        }
        for (String expectedEntry : expectedEntries) {
            assertTrue(
                    "Missing ZIP entry: " + expectedEntry,
                    entryNames.contains(expectedEntry)
            );
        }
    }

    private void assertEquivalent(
            ProjectLoadResultDataObject expected,
            ProjectLoadResultDataObject actual
    ) throws Exception {
        NodeContainerDataObject expectedNodes = expected.getNodeContainer();
        NodeContainerDataObject actualNodes = actual.getNodeContainer();

        assertEquals(
                expectedNodes.getProjectStructure().getProject().getProjectConfigVersion(),
                actualNodes.getProjectStructure().getProject().getProjectConfigVersion()
        );
        assertEquals(
                expectedNodes.getProjectStructure().getProject().getProjectName(),
                actualNodes.getProjectStructure().getProject().getProjectName()
        );
        assertEquals(expected.getGlobal().getStorages().size(), actual.getGlobal().getStorages().size());
        assertEquals(expected.getGlobal().getServices().size(), actual.getGlobal().getServices().size());
        assertEquals(expectedNodes.getNodeGlobals().size(), actualNodes.getNodeGlobals().size());
        assertEquals(expectedNodes.getPipelines().size(), actualNodes.getPipelines().size());

        List<GraphNodeReferenceDataObject> expectedReferences =
                expectedNodes.getProjectStructure().getNodeReferences();
        List<GraphNodeReferenceDataObject> actualReferences =
                actualNodes.getProjectStructure().getNodeReferences();
        assertEquals(expectedReferences.size(), actualReferences.size());

        for (int index = 0; index < expectedReferences.size(); index++) {
            NodeDataObject expectedNode = expectedNodes.getProjectStructure()
                    .getNode(expectedReferences.get(index));
            NodeDataObject actualNode = actualNodes.getProjectStructure()
                    .getNode(actualReferences.get(index));
            assertEquals(expectedNode.getId(), actualNode.getId());
            assertEquals(expectedNode.getName(), actualNode.getName());
            assertEquals(expectedNode.getDescription(), actualNode.getDescription());
            assertEquals(expectedNode.getDownstream().size(), actualNode.getDownstream().size());
            assertEquals(
                    expectedNodes.getNodeGlobal(expectedReferences.get(index)).getStorages().size(),
                    actualNodes.getNodeGlobal(actualReferences.get(index)).getStorages().size()
            );
            assertEquals(
                    expectedNodes.getPipeline(expectedReferences.get(index))
                            .getStagesConsistency().size(),
                    actualNodes.getPipeline(actualReferences.get(index))
                            .getStagesConsistency().size()
            );
        }

        assertTrue(Files.isRegularFile(targetFile(actual, "Project.json")));
        assertTrue(Files.isRegularFile(targetFile(actual, "Global.json")));
        assertRawConfigurationEquals(expected, actual, expectedReferences, actualReferences);
    }

    private void assertRawConfigurationEquals(
            ProjectLoadResultDataObject expected,
            ProjectLoadResultDataObject actual,
            List<GraphNodeReferenceDataObject> expectedReferences,
            List<GraphNodeReferenceDataObject> actualReferences
    ) throws Exception {
        GsonJsonFileReaderUtility reader = new GsonJsonFileReaderUtility();
        assertEquals(
                reader.read(targetFile(expected, "Project.json"), Project.class),
                reader.read(targetFile(actual, "Project.json"), Project.class)
        );
        assertEquals(
                reader.read(targetFile(expected, "Global.json"), Global.class),
                reader.read(targetFile(actual, "Global.json"), Global.class)
        );

        NodePathResolverStrategy pathResolver = new NodePathResolverStrategy();
        for (int index = 0; index < expectedReferences.size(); index++) {
            NodeDataObject expectedNode = expected.getNodeContainer().getProjectStructure()
                    .getNode(expectedReferences.get(index));
            NodeDataObject actualNode = actual.getNodeContainer().getProjectStructure()
                    .getNode(actualReferences.get(index));
            Path expectedNodeDirectory = pathResolver.resolve(
                    expected.getProjectDirectory(),
                    expectedNode
            );
            Path actualNodeDirectory = pathResolver.resolve(
                    actual.getProjectDirectory(),
                    actualNode
            );
            assertEquals(
                    reader.read(expectedNodeDirectory.resolve("global.json"), NodeGlobal.class),
                    reader.read(actualNodeDirectory.resolve("global.json"), NodeGlobal.class)
            );
            assertEquals(
                    reader.read(expectedNodeDirectory.resolve("pipeline.json"), Pipeline.class),
                    reader.read(actualNodeDirectory.resolve("pipeline.json"), Pipeline.class)
            );
        }
    }

    private Path targetFile(ProjectLoadResultDataObject project, String fileName) {
        return project.getProjectDirectory().resolve("data").resolve(fileName);
    }

    private void deleteTree(Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }
        Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
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
}
