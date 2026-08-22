package io.github.byzatic.tessera.lib.configio.infrastructure.factory;

import io.github.byzatic.tessera.lib.configio.application.service.ServiceSaverInterface;
import io.github.byzatic.tessera.lib.configio.infrastructure.saver.ServiceSaverStrategy;

public final class ServiceSaverFactory {

    private ServiceSaverFactory() {
    }

    public static ServiceSaverInterface create() {
        return new ServiceSaverStrategy();
    }
}
