package io.github.byzatic.lib.configio.unified.internal;

import io.github.byzatic.lib.configio.application.loader.ProjectLoaderInterface;
import io.github.byzatic.lib.configio.application.revision.ProjectRevisionFailure;
import io.github.byzatic.lib.configio.application.revision.ProjectRevisionSource;
import io.github.byzatic.lib.configio.application.saver.ProjectExporterInterface;
import io.github.byzatic.lib.configio.application.saver.ProjectSaverInterface;
import io.github.byzatic.lib.configio.domain.model.ProjectExportDataObject;
import io.github.byzatic.lib.configio.domain.model.ProjectLoadResultDataObject;
import io.github.byzatic.lib.configio.infrastructure.factory.ProjectV1ExporterFactory;
import io.github.byzatic.lib.configio.infrastructure.factory.ProjectV1LoaderFactory;
import io.github.byzatic.lib.configio.infrastructure.factory.ProjectV1SaverFactory;
import io.github.byzatic.lib.configio.infrastructure.revision.PollingZipProjectRevisionSource;
import io.github.byzatic.lib.configio.infrastructure.revision.ZipProjectRevisionSourceConfiguration;
import io.github.byzatic.lib.configio.unified.ProjectRevisionError;
import io.github.byzatic.lib.configio.unified.ProjectRevisionHandle;
import io.github.byzatic.lib.configio.unified.ProjectRevisionListener;
import io.github.byzatic.lib.configio.unified.ProjectRevisionSubscription;
import io.github.byzatic.lib.configio.unified.ProjectRevisionWatchRequest;
import io.github.byzatic.lib.configio.unified.ProjectRuntimeSession;
import io.github.byzatic.lib.configio.unified.TesseraProjectException;
import io.github.byzatic.lib.configio.unified.TesseraProjectIO;
import io.github.byzatic.lib.configio.unified.TesseraProjectOperation;
import io.github.byzatic.lib.configio.unified.model.ExportProjectRequest;
import io.github.byzatic.lib.configio.unified.model.SaveProjectRequest;
import io.github.byzatic.lib.configio.unified.model.SaveProjectResult;
import io.github.byzatic.lib.configio.unified.model.TesseraProject;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/** Standard adapter that composes the existing version-one config-io implementation. */
public final class DefaultTesseraProjectIO implements TesseraProjectIO {

    private final List<ClassLoader> preloadedClassLoaders;
    private final LegacyProjectMapper mapper = new LegacyProjectMapper();

    /**
     * Creates a facade that discovers runtime extensions from the project itself.
     *
     * @return a default unified project facade
     */
    public static TesseraProjectIO createDefault() {
        return new DefaultTesseraProjectIO(List.of());
    }

    /**
     * Creates a facade with host-provided class loaders available to runtime discovery.
     *
     * @param preloadedClassLoaders class loaders supplied by the embedding application
     * @return a configured unified project facade
     */
    public static TesseraProjectIO createWithPreloadedClassLoaders(
            List<ClassLoader> preloadedClassLoaders
    ) {
        return new DefaultTesseraProjectIO(preloadedClassLoaders);
    }

    /**
     * Creates the standard adapter over the existing version-one implementation.
     *
     * @param preloadedClassLoaders class loaders supplied by the embedding application
     */
    public DefaultTesseraProjectIO(List<ClassLoader> preloadedClassLoaders) {
        this.preloadedClassLoaders = List.copyOf(
                Objects.requireNonNull(preloadedClassLoaders, "preloadedClassLoaders")
        );
        if (this.preloadedClassLoaders.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Preloaded class loaders must not contain null");
        }
    }

    @Override
    public TesseraProject loadProject(Path projectDirectory)
            throws TesseraProjectException {
        Path location = normalize(projectDirectory, "projectDirectory");
        ProjectLoaderInterface loader = createLoader();
        try (ProjectLoadResultDataObject loadedProject = loader.load(location)) {
            return mapper.toUnified(loadedProject);
        } catch (Exception exception) {
            throw failure(
                    TesseraProjectOperation.LOAD_PROJECT,
                    location,
                    "Cannot load project: " + location,
                    exception
            );
        }
    }

    @Override
    public SaveProjectResult saveProject(SaveProjectRequest request)
            throws TesseraProjectException {
        Objects.requireNonNull(request, "request");
        Path location = request.getProjectDirectory();
        try {
            LegacyProjectParts project = mapper.toLegacy(request.getProject());
            ProjectSaverInterface saver = ProjectV1SaverFactory.create();
            Path archive = saver.save(
                    location,
                    project.getGlobal(),
                    project.getNodeContainer(),
                    request.getArtifacts().getRoutineJars(),
                    request.getArtifacts().getServiceJars(),
                    mapper.toLegacyDslSources(request.getArtifacts().getDslSources())
            );
            return SaveProjectResult.newBuilder()
                    .projectDirectory(location)
                    .archive(archive)
                    .build();
        } catch (Exception exception) {
            throw failure(
                    TesseraProjectOperation.SAVE_PROJECT,
                    location,
                    "Cannot save project: " + location,
                    exception
            );
        }
    }

    @Override
    public Path exportProject(ExportProjectRequest request)
            throws TesseraProjectException {
        Objects.requireNonNull(request, "request");
        Path location = request.getArchiveDestination();
        try {
            LegacyProjectParts project = mapper.toLegacy(request.getProject());
            ProjectExporterInterface exporter = ProjectV1ExporterFactory.create();
            return exporter.export(new ProjectExportDataObject(
                    location,
                    project.getGlobal(),
                    project.getNodeContainer(),
                    request.getArtifacts().getRoutineJars(),
                    request.getArtifacts().getServiceJars(),
                    mapper.toLegacyDslSources(request.getArtifacts().getDslSources())
            ));
        } catch (Exception exception) {
            throw failure(
                    TesseraProjectOperation.EXPORT_PROJECT,
                    location,
                    "Cannot export project: " + location,
                    exception
            );
        }
    }

    @Override
    public ProjectRuntimeSession openRuntime(Path projectDirectory)
            throws TesseraProjectException {
        Path location = normalize(projectDirectory, "projectDirectory");
        ProjectLoadResultDataObject loadedProject = null;
        try {
            loadedProject = createLoader().load(location);
            TesseraProject project = mapper.toUnified(loadedProject);
            ProjectRuntimeSession session = DefaultProjectRuntimeSession.open(
                    loadedProject,
                    project,
                    true
            );
            loadedProject = null;
            return session;
        } catch (TesseraProjectException exception) {
            closeAfterFailure(loadedProject, exception);
            throw exception;
        } catch (Exception exception) {
            closeAfterFailure(loadedProject, exception);
            throw failure(
                    TesseraProjectOperation.OPEN_RUNTIME,
                    location,
                    "Cannot open project runtime: " + location,
                    exception
            );
        }
    }

    @Override
    public ProjectRevisionSubscription watchRevisions(
            ProjectRevisionWatchRequest request,
            ProjectRevisionListener listener
    ) throws TesseraProjectException {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(listener, "listener");
        ZipProjectRevisionSourceConfiguration configuration =
                ZipProjectRevisionSourceConfiguration.newBuilder()
                        .sourceArchive(request.getSourceArchive())
                        .stagingDirectory(request.getStagingDirectory())
                        .pollInterval(request.getPollInterval())
                        .stableObservationCount(request.getStableObservationCount())
                        .maximumEntryCount(request.getMaximumEntryCount())
                        .maximumExpandedBytes(request.getMaximumExpandedBytes())
                        .build();
        ProjectRevisionSource source = new PollingZipProjectRevisionSource(
                configuration,
                createLoader()
        );
        try {
            source.start(new RevisionListenerAdapter(listener, mapper));
            return new DefaultProjectRevisionSubscription(source);
        } catch (Exception exception) {
            source.close();
            throw failure(
                    TesseraProjectOperation.WATCH_REVISIONS,
                    request.getSourceArchive(),
                    "Cannot watch project revisions: " + request.getSourceArchive(),
                    exception
            );
        }
    }

    private ProjectLoaderInterface createLoader() {
        return ProjectV1LoaderFactory.create(preloadedClassLoaders);
    }

    private Path normalize(Path path, String name) {
        return Objects.requireNonNull(path, name).toAbsolutePath().normalize();
    }

    private void closeAfterFailure(ProjectLoadResultDataObject project, Throwable failure) {
        if (project == null) {
            return;
        }
        try {
            project.close();
        } catch (IOException exception) {
            failure.addSuppressed(exception);
        }
    }

    private TesseraProjectException failure(
            TesseraProjectOperation operation,
            Path location,
            String message,
            Throwable cause
    ) {
        return new TesseraProjectException(operation, location, message, cause);
    }

    private static final class RevisionListenerAdapter
            implements io.github.byzatic.lib.configio.application.revision.ProjectRevisionListener {

        private final ProjectRevisionListener listener;
        private final LegacyProjectMapper mapper;

        private RevisionListenerAdapter(
                ProjectRevisionListener listener,
                LegacyProjectMapper mapper
        ) {
            this.listener = listener;
            this.mapper = mapper;
        }

        @Override
        public void onRevisionAvailable(
                io.github.byzatic.lib.configio.application.revision.ProjectRevision revision
        ) {
            ProjectRevisionHandle handle = new DefaultProjectRevisionHandle(revision, mapper);
            try {
                listener.onRevisionAvailable(handle);
            } catch (RuntimeException exception) {
                try {
                    handle.close();
                } catch (IOException closeException) {
                    exception.addSuppressed(closeException);
                }
                throw exception;
            }
        }

        @Override
        public void onRevisionRejected(ProjectRevisionFailure failure) {
            listener.onRevisionRejected(ProjectRevisionError.newBuilder()
                    .sourceArchive(failure.getSourceArchive())
                    .revisionId(failure.getRevisionId())
                    .cause(failure.getCause())
                    .build());
        }
    }
}
