package io.github.byzatic.lib.configio.application.module;

import io.github.byzatic.lib.configio.domain.exception.PluginLoadingException;
import io.github.byzatic.tessera.workflowroutine.api_engine.MCg3WorkflowRoutineApiInterface;
import io.github.byzatic.tessera.workflowroutine.workflowroutines.WorkflowRoutineInterface;
import io.github.byzatic.tessera.workflowroutine.workflowroutines.health.HealthFlagProxy;

import java.util.Set;

public interface ModuleLoaderInterface extends AutoCloseable {

    WorkflowRoutineInterface getModule(
            String workflowRoutineClassName,
            MCg3WorkflowRoutineApiInterface workflowRoutineApi,
            HealthFlagProxy healthFlagProxy
    ) throws PluginLoadingException;

    Set<String> getAvailableModuleNames();

    @Override
    void close() throws PluginLoadingException;
}
