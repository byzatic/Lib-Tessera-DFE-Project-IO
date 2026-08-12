package io.github.byzatic.lib.configio.infrastructure.dao;

import io.github.byzatic.lib.configio.application.dao.ProjectGlobalDaoInterface;
import io.github.byzatic.lib.configio.domain.exception.ProjectLoadingException;
import io.github.byzatic.lib.configio.domain.model.ProjectGlobalDataObject;
import io.github.byzatic.lib.configio.infrastructure.dto.raw.global.Global;
import io.github.byzatic.lib.configio.infrastructure.util.ConfigurationDataMapperUtility;
import io.github.byzatic.lib.configio.infrastructure.util.GsonJsonFileReaderUtility;

import java.nio.file.Path;
import java.util.Objects;

public final class GsonProjectGlobalDao implements ProjectGlobalDaoInterface {

    private final GsonJsonFileReaderUtility jsonFileReader;
    private final ConfigurationDataMapperUtility dataMapper;

    public GsonProjectGlobalDao() {
        this(new GsonJsonFileReaderUtility(), new ConfigurationDataMapperUtility());
    }

    public GsonProjectGlobalDao(
            GsonJsonFileReaderUtility jsonFileReader,
            ConfigurationDataMapperUtility dataMapper
    ) {
        this.jsonFileReader = Objects.requireNonNull(jsonFileReader, "jsonFileReader");
        this.dataMapper = Objects.requireNonNull(dataMapper, "dataMapper");
    }

    @Override
    public ProjectGlobalDataObject load(Path projectDirectory) throws ProjectLoadingException {
        Global rawGlobal = jsonFileReader.read(
                projectDirectory.resolve("data").resolve("Global.json"),
                Global.class
        );
        return dataMapper.mapProjectGlobal(rawGlobal);
    }
}
