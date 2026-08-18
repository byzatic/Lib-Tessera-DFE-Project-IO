package io.github.byzatic.lib.configio.infrastructure.dao;

import io.github.byzatic.lib.configio.application.dao.ProjectDaoInterface;
import io.github.byzatic.lib.configio.domain.exception.ProjectLoadingException;
import io.github.byzatic.lib.configio.domain.exception.ProjectSavingException;
import io.github.byzatic.lib.configio.domain.model.GraphNodeReferenceDataObject;
import io.github.byzatic.lib.configio.domain.model.NodeDataObject;
import io.github.byzatic.lib.configio.domain.model.ProjectDataObject;
import io.github.byzatic.lib.configio.domain.model.ProjectStructureDataObject;
import io.github.byzatic.lib.configio.infrastructure.dto.raw.project_and_graph.Project;
import io.github.byzatic.lib.configio.infrastructure.util.GsonJsonFileReaderUtility;
import io.github.byzatic.lib.configio.infrastructure.util.ProjectStructureFlattenerUtility;
import io.github.byzatic.lib.configio.infrastructure.utils.GsonJsonFileWriterUtility;
import io.github.byzatic.lib.configio.infrastructure.utils.ProjectDirectoryInitializerUtility;
import io.github.byzatic.lib.configio.infrastructure.utils.ProjectStructureMapper;

import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

public final class GsonProjectDao implements ProjectDaoInterface {

    private final GsonJsonFileReaderUtility jsonFileReader;
    private final ProjectStructureFlattenerUtility structureFlattener;
    private final GsonJsonFileWriterUtility jsonFileWriter;
    private final ProjectStructureMapper structureMapper;
    private final ProjectDirectoryInitializerUtility directoryInitializer;

    public GsonProjectDao() {
        this(
                new GsonJsonFileReaderUtility(),
                new ProjectStructureFlattenerUtility(),
                new GsonJsonFileWriterUtility(),
                new ProjectStructureMapper(),
                new ProjectDirectoryInitializerUtility()
        );
    }

    public GsonProjectDao(
            GsonJsonFileReaderUtility jsonFileReader,
            ProjectStructureFlattenerUtility structureFlattener
    ) {
        this(
                jsonFileReader,
                structureFlattener,
                new GsonJsonFileWriterUtility(),
                new ProjectStructureMapper(),
                new ProjectDirectoryInitializerUtility()
        );
    }

    public GsonProjectDao(
            GsonJsonFileReaderUtility jsonFileReader,
            ProjectStructureFlattenerUtility structureFlattener,
            GsonJsonFileWriterUtility jsonFileWriter,
            ProjectStructureMapper structureMapper,
            ProjectDirectoryInitializerUtility directoryInitializer
    ) {
        this.jsonFileReader = Objects.requireNonNull(jsonFileReader, "jsonFileReader");
        this.structureFlattener = Objects.requireNonNull(
                structureFlattener,
                "structureFlattener"
        );
        this.jsonFileWriter = Objects.requireNonNull(jsonFileWriter, "jsonFileWriter");
        this.structureMapper = Objects.requireNonNull(structureMapper, "structureMapper");
        this.directoryInitializer = Objects.requireNonNull(
                directoryInitializer,
                "directoryInitializer"
        );
    }

    @Override
    public ProjectStructureDataObject load(Path projectDirectory)
            throws ProjectLoadingException {
        Path projectFile = projectDirectory.resolve("data").resolve("Project.json");
        Project project = jsonFileReader.read(projectFile, Project.class);
        Map<GraphNodeReferenceDataObject, NodeDataObject> nodes =
                structureFlattener.flatten(project.getStructure());
        ProjectDataObject projectData = new ProjectDataObject(
                project.getProjectConfigVersion(),
                project.getProjectName()
        );
        return new ProjectStructureDataObject(projectData, nodes);
    }

    @Override
    public void save(Path projectDirectory, ProjectStructureDataObject projectStructure)
            throws ProjectSavingException {
        directoryInitializer.initialize(projectDirectory);
        Path projectFile = projectDirectory.resolve("data").resolve("Project.json");
        jsonFileWriter.write(projectFile, structureMapper.map(projectStructure));
    }
}
