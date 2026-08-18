package io.github.byzatic.lib.configio.infrastructure.saver;

import io.github.byzatic.lib.configio.application.module.ModuleSaverInterface;
import io.github.byzatic.lib.configio.domain.exception.PluginSavingException;
import io.github.byzatic.lib.configio.infrastructure.utils.JarPluginSaverUtility;

import java.nio.file.Path;
import java.util.Objects;

public final class ModuleSaverStrategy implements ModuleSaverInterface {

    private static final Path MODULE_DIRECTORY =
            Path.of("modules", "workflow_routines");

    private final JarPluginSaverUtility pluginSaver;

    public ModuleSaverStrategy() {
        this(new JarPluginSaverUtility());
    }

    public ModuleSaverStrategy(JarPluginSaverUtility pluginSaver) {
        this.pluginSaver = Objects.requireNonNull(pluginSaver, "pluginSaver");
    }

    @Override
    public Path save(Path moduleJar, Path projectDirectory)
            throws PluginSavingException {
        return pluginSaver.save(moduleJar, projectDirectory, MODULE_DIRECTORY);
    }
}
