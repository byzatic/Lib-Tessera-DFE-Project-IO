package io.github.byzatic.tessera.lib.configio.infrastructure.dao;

import io.github.byzatic.tessera.lib.configio.application.dao.ProjectGlobalDaoInterface;
import io.github.byzatic.tessera.lib.configio.domain.exception.ProjectLoadingException;
import io.github.byzatic.tessera.lib.configio.domain.exception.ProjectSavingException;
import io.github.byzatic.tessera.lib.configio.domain.model.ProjectGlobalDataObject;
import io.github.byzatic.tessera.lib.configio.infrastructure.dto.raw.global.Global;
import io.github.byzatic.tessera.lib.configio.infrastructure.util.ConfigurationDataMapperUtility;
import io.github.byzatic.tessera.lib.configio.infrastructure.util.GsonJsonFileReaderUtility;
import io.github.byzatic.tessera.lib.configio.infrastructure.utils.GsonJsonFileWriterUtility;

import java.nio.file.Path;
import java.util.Objects;

public final class GsonProjectGlobalDao implements ProjectGlobalDaoInterface {

    private final GsonJsonFileReaderUtility jsonFileReader;
    private final ConfigurationDataMapperUtility dataMapper;
    private final GsonJsonFileWriterUtility jsonFileWriter;

    public GsonProjectGlobalDao() {
        this(
                new GsonJsonFileReaderUtility(),
                new ConfigurationDataMapperUtility(),
                new GsonJsonFileWriterUtility()
        );
    }

    public GsonProjectGlobalDao(
            GsonJsonFileReaderUtility jsonFileReader,
            ConfigurationDataMapperUtility dataMapper
    ) {
        this(jsonFileReader, dataMapper, new GsonJsonFileWriterUtility());
    }

    public GsonProjectGlobalDao(
            GsonJsonFileReaderUtility jsonFileReader,
            ConfigurationDataMapperUtility dataMapper,
            GsonJsonFileWriterUtility jsonFileWriter
    ) {
        this.jsonFileReader = Objects.requireNonNull(jsonFileReader, "jsonFileReader");
        this.dataMapper = Objects.requireNonNull(dataMapper, "dataMapper");
        this.jsonFileWriter = Objects.requireNonNull(jsonFileWriter, "jsonFileWriter");
    }

    @Override
    public ProjectGlobalDataObject load(Path projectDirectory) throws ProjectLoadingException {
        Global rawGlobal = jsonFileReader.read(
                projectDirectory.resolve("data").resolve("Global.json"),
                Global.class
        );
        return dataMapper.mapProjectGlobal(rawGlobal);
    }

    @Override
    public void save(Path projectDirectory, ProjectGlobalDataObject global)
            throws ProjectSavingException {
        Path globalFile = projectDirectory.resolve("data").resolve("Global.json");
        jsonFileWriter.write(globalFile, dataMapper.mapProjectGlobal(global));
    }
}
