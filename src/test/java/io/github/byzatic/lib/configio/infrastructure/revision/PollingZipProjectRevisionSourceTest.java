package io.github.byzatic.lib.configio.infrastructure.revision;

import io.github.byzatic.lib.configio.application.loader.ProjectLoaderInterface;
import io.github.byzatic.lib.configio.application.revision.ProjectRevision;
import io.github.byzatic.lib.configio.application.revision.ProjectRevisionFailure;
import io.github.byzatic.lib.configio.application.revision.ProjectRevisionListener;
import io.github.byzatic.lib.configio.domain.exception.ProjectLoadingException;
import io.github.byzatic.lib.configio.domain.model.NodeContainerDataObject;
import io.github.byzatic.lib.configio.domain.model.ProjectDataObject;
import io.github.byzatic.lib.configio.domain.model.ProjectGlobalDataObject;
import io.github.byzatic.lib.configio.domain.model.ProjectLoadResultDataObject;
import io.github.byzatic.lib.configio.domain.model.ProjectStructureDataObject;
import io.github.byzatic.lib.configio.domain.model.SharedResourcesContainerDataObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PollingZipProjectRevisionSourceTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void publishesLoadedRevisionAndDeletesStagingDirectoryOnClose() throws Exception {
        Path root = temporaryFolder.newFolder("valid-revision").toPath();
        Path archive = root.resolve("project.zip");
        writeZip(archive, "Demo/data/Project.json", "{}");

        CapturingListener listener = new CapturingListener();
        PollingZipProjectRevisionSource source = createSource(root, archive);
        try {
            source.start(listener);
            assertTrue(listener.awaitRevision());

            ProjectRevision revision = listener.getRevision();
            assertNotNull(revision);
            assertTrue(Files.isRegularFile(
                    revision.getProjectDirectory().resolve("data").resolve("Project.json")
            ));
            Path projectDirectory = revision.getProjectDirectory();
            revision.close();
            assertFalse(Files.exists(projectDirectory));
        } finally {
            source.close();
        }
    }

    @Test
    public void rejectsArchiveEntryOutsideStagingDirectory() throws Exception {
        Path root = temporaryFolder.newFolder("zip-slip").toPath();
        Path archive = root.resolve("project.zip");
        writeZip(archive, "../escaped.txt", "forbidden");

        CapturingListener listener = new CapturingListener();
        PollingZipProjectRevisionSource source = createSource(root, archive);
        try {
            source.start(listener);
            assertTrue(listener.awaitFailure());
            assertNotNull(listener.getFailure());
            assertFalse(Files.exists(root.resolve("escaped.txt")));
        } finally {
            source.close();
        }
    }

    private PollingZipProjectRevisionSource createSource(Path root, Path archive) {
        ZipProjectRevisionSourceConfiguration configuration =
                ZipProjectRevisionSourceConfiguration.newBuilder()
                        .sourceArchive(archive)
                        .stagingDirectory(root.resolve("staging"))
                        .pollInterval(Duration.ofMillis(20L))
                        .stableObservationCount(1)
                        .build();
        return new PollingZipProjectRevisionSource(configuration, new StubProjectLoader());
    }

    private void writeZip(Path archive, String entryName, String contents) throws IOException {
        try (OutputStream fileOutput = Files.newOutputStream(archive);
             ZipOutputStream zipOutput = new ZipOutputStream(fileOutput)) {
            zipOutput.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));
            zipOutput.write("Manifest-Version: 1.0\n".getBytes(StandardCharsets.UTF_8));
            zipOutput.closeEntry();
            zipOutput.putNextEntry(new ZipEntry(entryName));
            zipOutput.write(contents.getBytes(StandardCharsets.UTF_8));
            zipOutput.closeEntry();
        }
    }

    private static final class StubProjectLoader implements ProjectLoaderInterface {
        @Override
        public ProjectLoadResultDataObject load(Path projectDirectory)
                throws ProjectLoadingException {
            ProjectDataObject project = new ProjectDataObject("v1", "Demo");
            ProjectStructureDataObject structure = new ProjectStructureDataObject(
                    project,
                    new HashMap<>()
            );
            NodeContainerDataObject nodes = new NodeContainerDataObject(
                    structure,
                    new HashMap<>(),
                    new HashMap<>()
            );
            ProjectGlobalDataObject global = new ProjectGlobalDataObject(
                    new ArrayList<>(),
                    new ArrayList<>()
            );
            SharedResourcesContainerDataObject resources =
                    new SharedResourcesContainerDataObject(
                            new ArrayList<>(),
                            new ArrayList<>()
                    );
            return new ProjectLoadResultDataObject(
                    projectDirectory,
                    global,
                    nodes,
                    resources
            );
        }
    }

    private static final class CapturingListener implements ProjectRevisionListener {

        private final CountDownLatch revisionLatch = new CountDownLatch(1);
        private final CountDownLatch failureLatch = new CountDownLatch(1);
        private final AtomicReference<ProjectRevision> revision = new AtomicReference<>();
        private final AtomicReference<ProjectRevisionFailure> failure = new AtomicReference<>();

        @Override
        public void onRevisionAvailable(ProjectRevision value) {
            revision.set(value);
            revisionLatch.countDown();
        }

        @Override
        public void onRevisionRejected(ProjectRevisionFailure value) {
            failure.set(value);
            failureLatch.countDown();
        }

        private boolean awaitRevision() throws InterruptedException {
            return revisionLatch.await(5L, TimeUnit.SECONDS);
        }

        private boolean awaitFailure() throws InterruptedException {
            return failureLatch.await(5L, TimeUnit.SECONDS);
        }

        private ProjectRevision getRevision() {
            return revision.get();
        }

        private ProjectRevisionFailure getFailure() {
            return failure.get();
        }
    }
}
