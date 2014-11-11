package com.goodgame.profiling.rewrite_framework.systems.gatherer.optimization;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.goodgame.profiling.commons.systems.configuration.EnvironmentWithJSONConfiguration;

public class DisabledByConfigStrategy< E extends EnvironmentWithJSONConfiguration> implements ShouldRunOptimizerStrategy {
    private static final Logger log = LogManager.getLogger();

    private final E environment;

    public DisabledByConfigStrategy( E environment ) {
        this.environment = environment;
    }

    @Override
    public boolean shouldRun() {
        log.entry();
        return log.exit(!this.environment.getConfiguration().getJSONObject("threadcount-optimizer").optBoolean("disabled", false));
    }
}
