package io.github.byzatic.tessera.lib.configio.application.saver;

import io.github.byzatic.tessera.lib.configio.application.loader.ProjectLoaderInterface;
import io.github.byzatic.tessera.lib.configio.domain.model.DslFileDataObject;
import io.github.byzatic.tessera.lib.configio.domain.model.GraphNodeReferenceDataObject;
import io.github.byzatic.tessera.lib.configio.domain.model.ProjectExportDataObject;
import io.github.byzatic.tessera.lib.configio.domain.model.ProjectLoadResultDataObject;
import io.github.byzatic.tessera.lib.configio.infrastructure.factory.ProjectV1ExporterFactory;
import io.github.byzatic.tessera.lib.configio.infrastructure.factory.ProjectV1LoaderFactory;
import io.github.byzatic.tessera.lib.configio.support.TestProjectFixture;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ProjectV1ExporterStrategyTest {

    @Test
    public void shouldExportOnlyZipWithMcg3DslFile() throws Exception {
        Path testDirectory = Files.createTempDirectory("project-v1-exporter-test-");
        Path destination = testDirectory.resolve("delivery.zip");
        ProjectLoaderInterface loader = ProjectV1LoaderFactory.create();
        ProjectExporterInterface exporter = ProjectV1ExporterFactory.create();

        try (TestProjectFixture fixture = TestProjectFixture.create();
             ProjectLoadResultDataObject source = loader.load(fixture.getProjectDirectory())) {
            GraphNodeReferenceDataObject reference = source.getNodeContainer()
                    .getProjectStructure().getNodeReferences().get(0);
            DslFileDataObject dslFile = new DslFileDataObject(reference, "worker", "select *");
            ProjectExportDataObject request = new ProjectExportDataObject(
                    destination,
                    source.getGlobal(),
                    source.getNodeContainer(),
                    Collections.<Path>emptyList(),
                    Collections.<Path>emptyList(),
                    Collections.singletonList(dslFile)
            );

            assertEquals(destination, exporter.export(request));
            assertTrue(Files.isRegularFile(destination));
            assertFalse(Files.exists(testDirectory.resolve("delivery")));

            try (ZipFile archive = new ZipFile(destination.toFile(), StandardCharsets.UTF_8)) {
                ZipEntry dslEntry = findBySuffix(archive, "/configuration_files/worker.mcg3dsl");
                assertNotNull(dslEntry);
                assertEquals("select *", new String(
                        archive.getInputStream(dslEntry).readAllBytes(), StandardCharsets.UTF_8));
            }
        } finally {
            deleteTree(testDirectory);
        }
    }

    private ZipEntry findBySuffix(ZipFile archive, String suffix) {
        Enumeration<? extends ZipEntry> entries = archive.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.getName().endsWith(suffix)) return entry;
        }
        return null;
    }

    private void deleteTree(Path root) throws Exception {
        if (!Files.exists(root)) return;
        try (var paths = Files.walk(root)) {
            for (Path path : paths.sorted(java.util.Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        }
    }
}
