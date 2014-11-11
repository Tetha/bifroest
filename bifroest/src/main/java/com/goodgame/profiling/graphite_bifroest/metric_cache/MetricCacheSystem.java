package com.goodgame.profiling.graphite_bifroest.metric_cache;

import java.util.Arrays;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.goodgame.profiling.commons.boot.interfaces.Subsystem;
import com.goodgame.profiling.commons.systems.SystemIdentifiers;
import com.goodgame.profiling.commons.systems.configuration.EnvironmentWithJSONConfiguration;
import com.goodgame.profiling.graphite_bifroest.systems.BifroestIdentifiers;
import com.goodgame.profiling.graphite_bifroest.systems.cassandra.EnvironmentWithCassandra;
import com.goodgame.profiling.graphite_retentions.bootloader.EnvironmentWithRetentionStrategy;

public class MetricCacheSystem<E extends EnvironmentWithRetentionStrategy & EnvironmentWithJSONConfiguration & EnvironmentWithCassandra & EnvironmentWithMutableMetricCache> implements Subsystem<E> {
    private static final Logger log = LogManager.getLogger();

    @Override
    public String getSystemIdentifier() {
        return BifroestIdentifiers.METRIC_CACHE;
    }

    @Override
    public Collection<String> getRequiredSystems() {
        return Arrays.asList( SystemIdentifiers.RETENTION, SystemIdentifiers.CONFIGURATION, BifroestIdentifiers.CASSANDRA, SystemIdentifiers.STATISTICS, SystemIdentifiers.LOGGING );
    }

    @Override
    public void boot( E environment ) throws Exception {
        environment.getConfigurationLoader().subscribe( config -> {
            log.warn( "Ignoring config reload! This is seriously broken!" );
        });

        environment.setMetricCache( new MetricCache( environment.cassandraAccessLayer(), environment.retentions() ) );
    }

    @Override
    public void shutdown( E environment ) {
        environment.setMetricCache( null );
    }
}
