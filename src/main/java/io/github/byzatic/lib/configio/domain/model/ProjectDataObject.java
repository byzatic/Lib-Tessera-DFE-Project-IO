package io.github.byzatic.lib.configio.domain.model;

import java.util.Objects;

public final class ProjectDataObject {

    private final String projectConfigVersion;
    private final String projectName;

    public ProjectDataObject(String projectConfigVersion, String projectName) {
        this.projectConfigVersion = Objects.requireNonNull(
                projectConfigVersion,
                "projectConfigVersion"
        );
        this.projectName = Objects.requireNonNull(projectName, "projectName");
    }

    public String getProjectConfigVersion() {
        return projectConfigVersion;
    }

    public String getProjectName() {
        return projectName;
    }
}
