package io.github.byzatic.lib.configio.unified;

import io.github.byzatic.lib.configio.unified.model.ExportProjectRequest;
import io.github.byzatic.lib.configio.unified.model.SaveProjectRequest;
import io.github.byzatic.lib.configio.unified.model.SaveProjectResult;
import io.github.byzatic.lib.configio.unified.model.TesseraProject;

import java.nio.file.Path;

/**
 * Unified entry point for loading, saving, exporting, running, and watching Tessera projects.
 *
 * <p>The facade itself does not own resources. Runtime sessions, revisions, and revision
 * subscriptions returned by it have explicit lifecycles and must be closed by their owners.</p>
 */
public interface TesseraProjectIO {

    /**
     * Loads a project as a detached immutable aggregate.
     *
     * @param projectDirectory existing project root directory
     * @return loaded project that does not own class loaders or other resources
     * @throws TesseraProjectException when the project cannot be read or mapped
     */
    TesseraProject loadProject(Path projectDirectory) throws TesseraProjectException;

    /**
     * Saves a project directory, optional artifacts, and the companion ZIP archive.
     *
     * @param request complete save command
     * @return normalized locations of the saved directory and archive
     * @throws TesseraProjectException when validation, writing, or archiving fails
     */
    SaveProjectResult saveProject(SaveProjectRequest request) throws TesseraProjectException;

    /**
     * Exports a project directly to a destination ZIP archive.
     *
     * @param request complete export command
     * @return normalized destination archive path
     * @throws TesseraProjectException when the project cannot be exported
     */
    Path exportProject(ExportProjectRequest request) throws TesseraProjectException;

    /**
     * Loads a project together with project-scoped runtime plugin resources.
     *
     * @param projectDirectory existing project root directory
     * @return runtime session owned by the caller
     * @throws TesseraProjectException when project or plugin loading fails
     */
    ProjectRuntimeSession openRuntime(Path projectDirectory) throws TesseraProjectException;

    /**
     * Starts asynchronous observation of stable project ZIP revisions.
     *
     * @param request polling and extraction policy
     * @param listener revision consumer that accepts ownership of delivered handles
     * @return running subscription owned by the caller
     * @throws TesseraProjectException when revision observation cannot be started
     */
    ProjectRevisionSubscription watchRevisions(
            ProjectRevisionWatchRequest request,
            ProjectRevisionListener listener
    ) throws TesseraProjectException;
}
