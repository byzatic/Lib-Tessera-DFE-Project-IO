package io.github.byzatic.lib.configio.example;

import io.github.byzatic.lib.configio.application.loader.ProjectLoaderInterface;
import io.github.byzatic.lib.configio.domain.exception.ProjectLoadingException;
import io.github.byzatic.lib.configio.domain.model.GraphNodeReferenceDataObject;
import io.github.byzatic.lib.configio.domain.model.NodeContainerDataObject;
import io.github.byzatic.lib.configio.domain.model.NodeDataObject;
import io.github.byzatic.lib.configio.domain.model.NodeGlobalDataObject;
import io.github.byzatic.lib.configio.domain.model.PipelineDataObject;
import io.github.byzatic.lib.configio.domain.model.ProjectGlobalDataObject;
import io.github.byzatic.lib.configio.domain.model.ProjectLoadResultDataObject;
import io.github.byzatic.lib.configio.domain.model.ProjectStructureDataObject;
import io.github.byzatic.lib.configio.domain.model.ServiceDataObject;
import io.github.byzatic.lib.configio.domain.model.StageConsistencyDataObject;
import io.github.byzatic.lib.configio.domain.model.StorageDataObject;
import io.github.byzatic.lib.configio.infrastructure.factory.ProjectV1LoaderFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

public final class ProjectLoaderExampleApplication {

    private static final Path DEFAULT_PROJECT_DIRECTORY =
            Path.of(".develop", "MyAwsomeProject");

    private final ProjectLoaderInterface projectLoader;
    private final BufferedReader input;
    private final PrintStream output;

    public ProjectLoaderExampleApplication(
            ProjectLoaderInterface projectLoader,
            BufferedReader input,
            PrintStream output
    ) {
        if (projectLoader == null) {
            throw new IllegalArgumentException("projectLoader must not be null");
        }
        if (input == null) {
            throw new IllegalArgumentException("input must not be null");
        }
        if (output == null) {
            throw new IllegalArgumentException("output must not be null");
        }
        this.projectLoader = projectLoader;
        this.input = input;
        this.output = output;
    }

    public static void main(String[] arguments) {
        Path projectDirectory = resolveProjectDirectory(arguments);
        ProjectLoaderInterface projectLoader = ProjectV1LoaderFactory.create();
        BufferedReader input = new BufferedReader(
                new InputStreamReader(System.in, StandardCharsets.UTF_8)
        );
        ProjectLoaderExampleApplication application =
                new ProjectLoaderExampleApplication(projectLoader, input, System.out);

        try {
            application.run(projectDirectory);
        } catch (ProjectLoadingException exception) {
            System.err.println("Project loading failed: " + exception.getMessage());
            System.exit(1);
        } catch (IOException exception) {
            System.err.println("Application input failed: " + exception.getMessage());
            System.exit(1);
        }
    }

    public void run(Path projectDirectory) throws ProjectLoadingException, IOException {
        try (ProjectLoadResultDataObject loadedProject =
                     projectLoader.load(projectDirectory)) {
            output.println("Project loaded from: " + loadedProject.getProjectDirectory());
            runMenu(loadedProject);
        }
        output.println("Project resources have been closed.");
    }

    private void runMenu(ProjectLoadResultDataObject loadedProject) throws IOException {
        boolean running = true;
        while (running) {
            printMenu();
            String command = readLine("Select action: ");

            if ("1".equals(command)) {
                printProjectSummary(loadedProject);
            } else if ("2".equals(command)) {
                printNodes(loadedProject.getNodeContainer());
            } else if ("3".equals(command)) {
                printSelectedNode(loadedProject.getNodeContainer());
            } else if ("4".equals(command)) {
                printGlobalConfiguration(loadedProject.getGlobal());
            } else if ("5".equals(command)) {
                printSharedResources(loadedProject);
            } else if ("0".equals(command)) {
                running = false;
            } else {
                output.println("Unknown action: " + command);
            }
            output.println();
        }
    }

    private void printMenu() {
        output.println();
        output.println("1 - Project summary");
        output.println("2 - List nodes");
        output.println("3 - Show node details");
        output.println("4 - Show global configuration");
        output.println("5 - Show shared resources");
        output.println("0 - Exit");
    }

    private void printProjectSummary(ProjectLoadResultDataObject loadedProject) {
        ProjectStructureDataObject structure =
                loadedProject.getNodeContainer().getProjectStructure();
        output.println("Project name: " + structure.getProject().getProjectName());
        output.println("Configuration version: "
                + structure.getProject().getProjectConfigVersion());
        output.println("Nodes: " + structure.getNodes().size());
        output.println("Global storages: " + loadedProject.getGlobal().getStorages().size());
        output.println("Global services: " + loadedProject.getGlobal().getServices().size());
    }

    private void printNodes(NodeContainerDataObject nodeContainer) {
        ProjectStructureDataObject structure = nodeContainer.getProjectStructure();
        List<GraphNodeReferenceDataObject> references = structure.getNodeReferences();

        for (int index = 0; index < references.size(); index++) {
            NodeDataObject node = structure.getNode(references.get(index));
            output.println(index + " - " + node.getName() + " [" + node.getId() + "]");
        }
    }

    private void printSelectedNode(NodeContainerDataObject nodeContainer) throws IOException {
        List<GraphNodeReferenceDataObject> references =
                nodeContainer.getProjectStructure().getNodeReferences();
        String rawIndex = readLine("Node index: ");
        int index;

        try {
            index = Integer.parseInt(rawIndex);
        } catch (NumberFormatException exception) {
            output.println("Node index must be an integer.");
            return;
        }

        if (index < 0 || index >= references.size()) {
            output.println("Node index is out of range: " + index);
            return;
        }

        GraphNodeReferenceDataObject reference = references.get(index);
        NodeDataObject node = nodeContainer.getProjectStructure().getNode(reference);
        NodeGlobalDataObject nodeGlobal = nodeContainer.getNodeGlobal(reference);
        PipelineDataObject pipeline = nodeContainer.getPipeline(reference);

        output.println("Runtime UUID: " + node.getUuid());
        output.println("ID: " + node.getId());
        output.println("Name: " + node.getName());
        output.println("Description: " + node.getDescription());
        output.println("Downstream nodes: " + node.getDownstream().size());
        printNodeStorages(nodeGlobal);
        printPipeline(pipeline);
    }

    private void printNodeStorages(NodeGlobalDataObject nodeGlobal) {
        output.println("Node storages:");
        List<StorageDataObject> storages = nodeGlobal.getStorages();
        for (StorageDataObject storage : storages) {
            output.println("  - " + storage.getIdName()
                    + " (options=" + storage.getOptions().size() + ")");
        }
    }

    private void printPipeline(PipelineDataObject pipeline) {
        output.println("Pipeline stages:");
        List<StageConsistencyDataObject> stages = pipeline.getStagesConsistency();
        for (StageConsistencyDataObject stage : stages) {
            output.println("  - position=" + stage.getPosition()
                    + ", id=" + stage.getStageId());
        }
    }

    private void printGlobalConfiguration(ProjectGlobalDataObject global) {
        output.println("Global storages:");
        for (StorageDataObject storage : global.getStorages()) {
            output.println("  - " + storage.getIdName()
                    + " (options=" + storage.getOptions().size() + ")");
        }

        output.println("Global services:");
        for (ServiceDataObject service : global.getServices()) {
            output.println("  - " + service.getIdName()
                    + " (options=" + service.getOptions().size() + ")");
        }
    }

    private void printSharedResources(ProjectLoadResultDataObject loadedProject) {
        List<ClassLoader> classLoaders = loadedProject
                .getSharedResourcesContainer()
                .getClassLoaders();
        output.println("Shared resource classloaders: " + classLoaders.size());
        for (int index = 0; index < classLoaders.size(); index++) {
            output.println("  " + index + " - " + classLoaders.get(index));
        }
    }

    private String readLine(String prompt) throws IOException {
        output.print(prompt);
        String value = input.readLine();
        if (value == null) {
            return "0";
        }
        return value.trim();
    }

    private static Path resolveProjectDirectory(String[] arguments) {
        if (arguments != null && arguments.length > 0) {
            return Path.of(arguments[0]).toAbsolutePath().normalize();
        }
        return DEFAULT_PROJECT_DIRECTORY.toAbsolutePath().normalize();
    }
}
