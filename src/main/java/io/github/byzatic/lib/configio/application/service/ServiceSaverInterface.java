package io.github.byzatic.lib.configio.application.service;

import io.github.byzatic.lib.configio.domain.exception.PluginSavingException;

import java.nio.file.Path;

public interface ServiceSaverInterface {

    Path save(Path serviceJar, Path projectDirectory) throws PluginSavingException;
}
