package io.github.byzatic.tessera.lib.configio.infrastructure.saver;

import io.github.byzatic.tessera.lib.configio.application.service.ServiceSaverInterface;
import io.github.byzatic.tessera.lib.configio.domain.exception.PluginSavingException;
import io.github.byzatic.tessera.lib.configio.infrastructure.utils.JarPluginSaverUtility;

import java.nio.file.Path;
import java.util.Objects;

public final class ServiceSaverStrategy implements ServiceSaverInterface {

    private static final Path SERVICE_DIRECTORY = Path.of("modules", "services");

    private final JarPluginSaverUtility pluginSaver;

    public ServiceSaverStrategy() {
        this(new JarPluginSaverUtility());
    }

    public ServiceSaverStrategy(JarPluginSaverUtility pluginSaver) {
        this.pluginSaver = Objects.requireNonNull(pluginSaver, "pluginSaver");
    }

    @Override
    public Path save(Path serviceJar, Path projectDirectory)
            throws PluginSavingException {
        return pluginSaver.save(serviceJar, projectDirectory, SERVICE_DIRECTORY);
    }
}
