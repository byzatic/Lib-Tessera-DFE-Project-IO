package io.github.byzatic.lib.configio.application.loader;

import io.github.byzatic.lib.configio.domain.model.GraphNodeReferenceDataObject;
import io.github.byzatic.lib.configio.domain.model.NodeContainerDataObject;
import io.github.byzatic.lib.configio.domain.model.ProjectLoadResultDataObject;
import io.github.byzatic.lib.configio.domain.model.ProjectStructureDataObject;
import io.github.byzatic.lib.configio.infrastructure.factory.ProjectV1LoaderFactory;
import io.github.byzatic.lib.configio.support.TestProjectFixture;
import org.junit.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ProjectV1LoaderStrategyTest {

    @Test
    public void shouldLoadCompleteProjectForRepository() throws Exception {
        ProjectLoaderInterface projectLoader = ProjectV1LoaderFactory.create();
        try (TestProjectFixture fixture = TestProjectFixture.create()) {
            Path projectDirectory = fixture.getProjectDirectory();
            ProjectLoadResultDataObject result = projectLoader.load(projectDirectory);
            try {
                NodeContainerDataObject nodeContainer = result.getNodeContainer();
                ProjectStructureDataObject projectStructure = nodeContainer.getProjectStructure();
                List<GraphNodeReferenceDataObject> nodeReferences =
                        projectStructure.getNodeReferences();

                assertEquals("test-project", projectStructure.getProject().getProjectName());
                assertEquals(1, nodeReferences.size());
                assertEquals(1, nodeContainer.getNodeGlobals().size());
                assertEquals(1, nodeContainer.getPipelines().size());
                assertEquals(1, result.getGlobal().getStorages().size());
                assertEquals(1, result.getGlobal().getServices().size());
                assertNotNull(nodeContainer.getNodeGlobal(nodeReferences.get(0)));
                assertNotNull(nodeContainer.getPipeline(nodeReferences.get(0)));
                assertEquals(
                        3,
                        nodeContainer.getNodeGlobal(nodeReferences.get(0)).getStorages().size()
                );
                assertEquals(
                        3,
                        nodeContainer.getPipeline(nodeReferences.get(0))
                                .getStagesConsistency().size()
                );
                assertNull(result.getSharedResourcesContainer().getLastClassLoader());
                assertFalse(result.getSharedResourcesContainer().isClosed());
            } finally {
                result.close();
            }

            assertTrue(result.getSharedResourcesContainer().isClosed());
        }
    }
}
