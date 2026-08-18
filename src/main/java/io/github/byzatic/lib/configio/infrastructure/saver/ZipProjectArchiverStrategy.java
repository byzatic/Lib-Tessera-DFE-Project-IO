package io.github.byzatic.lib.configio.infrastructure.saver;

import io.github.byzatic.lib.configio.application.saver.ProjectArchiverInterface;
import io.github.byzatic.lib.configio.domain.exception.ProjectSavingException;
import io.github.byzatic.lib.configio.infrastructure.utils.ZipArchiveWriterUtility;

import java.nio.file.Path;
import java.util.Objects;

public final class ZipProjectArchiverStrategy implements ProjectArchiverInterface {

    private final ZipArchiveWriterUtility archiveWriter;

    public ZipProjectArchiverStrategy() {
        this(new ZipArchiveWriterUtility());
    }

    public ZipProjectArchiverStrategy(ZipArchiveWriterUtility archiveWriter) {
        this.archiveWriter = Objects.requireNonNull(archiveWriter, "archiveWriter");
    }

    @Override
    public Path archive(Path projectDirectory) throws ProjectSavingException {
        return archiveWriter.archive(projectDirectory);
    }
}
