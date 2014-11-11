package com.goodgame.profiling.rewrite_framework.systems.gatherer;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;

import com.goodgame.profiling.commons.boot.interfaces.Subsystem;
import com.goodgame.profiling.commons.exception.ConfigurationException;
import com.goodgame.profiling.commons.systems.SystemIdentifiers;
import com.goodgame.profiling.commons.systems.configuration.EnvironmentWithJSONConfiguration;
import com.goodgame.profiling.commons.systems.cron.EnvironmentWithTaskRunner;
import com.goodgame.profiling.commons.systems.statistics.EnvironmentWithStatisticsGatherer;
import com.goodgame.profiling.rewrite_framework.systems.RewriteIdentifiers;
import com.goodgame.profiling.rewrite_framework.systems.persistent_drains.EnvironmentWithPersistentDrainManager;

public final class GathererSystem< E extends EnvironmentWithJSONConfiguration & EnvironmentWithTaskRunner & EnvironmentWithPersistentDrainManager & EnvironmentWithStatisticsGatherer, I, U >
        implements Subsystem<E> {

    private final Class<I> inputClass;
    private final Class<U> unitClass;

    private Fetcher<E, I, U> fetcher;

    public GathererSystem( Class<I> inputClass, Class<U> unitClass ) {
        this.inputClass = inputClass;
        this.unitClass = unitClass;
    }

    @Override
    public String getSystemIdentifier() {
        return RewriteIdentifiers.GATHERER;
    }

    @Override
    public void configure( E environment ) throws ConfigurationException {
        fetcher = new Fetcher<>( environment, inputClass, unitClass );
    }

    @Override
    public Collection<String> getRequiredSystems() {
        return Arrays.asList(SystemIdentifiers.STATISTICS,
                             RewriteIdentifiers.PERSISTENT_DRAINS );
    }

    @Override
    public void boot( E environment ) throws Exception {
        environment.taskRunner().runRepeated( fetcher, "Fetcher", Duration.ZERO, Duration.ofMinutes( 5 ), true );
    }

    @Override
    public void shutdown( E environment ) {
        fetcher.shutdown();
        fetcher = null;
    }
}
