package io.github.byzatic.lib.configio.infrastructure.factory;

import io.github.byzatic.lib.configio.application.module.ModuleSaverInterface;
import io.github.byzatic.lib.configio.infrastructure.saver.ModuleSaverStrategy;

public final class ModuleSaverFactory {

    private ModuleSaverFactory() {
    }

    public static ModuleSaverInterface create() {
        return new ModuleSaverStrategy();
    }
}
