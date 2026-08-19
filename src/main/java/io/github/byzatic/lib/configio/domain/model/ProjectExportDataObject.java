package io.github.byzatic.lib.configio.domain.model;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Complete input required to export a DFE project as a ZIP archive. */
public final class ProjectExportDataObject {

    private final Path archiveDestination;
    private final ProjectGlobalDataObject global;
    private final NodeContainerDataObject nodeContainer;
    private final List<Path> moduleJars;
    private final List<Path> serviceJars;
    private final List<DslFileDataObject> dslFiles;

    public ProjectExportDataObject(
            Path archiveDestination,
            ProjectGlobalDataObject global,
            NodeContainerDataObject nodeContainer,
            List<Path> moduleJars,
            List<Path> serviceJars,
            List<DslFileDataObject> dslFiles
    ) {
        this.archiveDestination = Objects.requireNonNull(archiveDestination, "archiveDestination");
        this.global = Objects.requireNonNull(global, "global");
        this.nodeContainer = Objects.requireNonNull(nodeContainer, "nodeContainer");
        this.moduleJars = immutableList(moduleJars, "moduleJars");
        this.serviceJars = immutableList(serviceJars, "serviceJars");
        this.dslFiles = immutableList(dslFiles, "dslFiles");
    }

    public Path getArchiveDestination() { return archiveDestination; }
    public ProjectGlobalDataObject getGlobal() { return global; }
    public NodeContainerDataObject getNodeContainer() { return nodeContainer; }
    public List<Path> getModuleJars() { return moduleJars; }
    public List<Path> getServiceJars() { return serviceJars; }
    public List<DslFileDataObject> getDslFiles() { return dslFiles; }

    private static <T> List<T> immutableList(List<T> values, String name) {
        Objects.requireNonNull(values, name);
        return Collections.unmodifiableList(new ArrayList<T>(values));
    }
}
