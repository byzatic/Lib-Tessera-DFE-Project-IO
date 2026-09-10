package io.github.byzatic.tessera.lib.configio.application.module;

import io.github.byzatic.tessera.lib.configio.domain.exception.PluginSavingException;

import java.nio.file.Path;

public interface ModuleSaverInterface {

    Path save(Path moduleJar, Path projectDirectory) throws PluginSavingException;
    /** Saves a shared-resource JAR under modules/shared. */
    default Path saveShared(Path sharedJar, Path projectDirectory) throws PluginSavingException {
        throw new PluginSavingException("Shared-resource JAR saving is not supported");
    }
}
