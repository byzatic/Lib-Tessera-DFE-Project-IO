package io.github.byzatic.lib.configio.application.module;

import io.github.byzatic.lib.configio.domain.exception.PluginSavingException;

import java.nio.file.Path;

public interface ModuleSaverInterface {

    Path save(Path moduleJar, Path projectDirectory) throws PluginSavingException;
}
