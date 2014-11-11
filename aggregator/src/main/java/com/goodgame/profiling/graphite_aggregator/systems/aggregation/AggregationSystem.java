package com.goodgame.profiling.graphite_aggregator.systems.aggregation;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.goodgame.profiling.commons.boot.interfaces.Subsystem;
import com.goodgame.profiling.commons.statistics.units.SI_PREFIX;
import com.goodgame.profiling.commons.statistics.units.TIME_UNIT;
import com.goodgame.profiling.commons.statistics.units.parse.TimeUnitParser;
import com.goodgame.profiling.commons.statistics.units.parse.UnitParser;
import com.goodgame.profiling.commons.systems.SystemIdentifiers;
import com.goodgame.profiling.commons.systems.configuration.EnvironmentWithJSONConfiguration;
import com.goodgame.profiling.commons.systems.cron.EnvironmentWithTaskRunner;
import com.goodgame.profiling.commons.systems.cron.TaskRunner;
import com.goodgame.profiling.graphite_aggregator.systems.AggregatorIdentifiers;
import com.goodgame.profiling.graphite_aggregator.systems.cassandra.EnvironmentWithCassandra;
import com.goodgame.profiling.graphite_retentions.bootloader.EnvironmentWithRetentionStrategy;

public class AggregationSystem< E extends EnvironmentWithJSONConfiguration & EnvironmentWithTaskRunner & EnvironmentWithRetentionStrategy & EnvironmentWithCassandra >
        implements Subsystem<E> {

    private static final int DEFAULT_POOLSIZE = 10;

    private static final Logger log = LogManager.getLogger();
    private static final UnitParser parser = new TimeUnitParser( SI_PREFIX.ONE, TIME_UNIT.SECOND );

    private Aggregator<E> aggregator;

    @Override
    public String getSystemIdentifier() {
        return AggregatorIdentifiers.AGGREGATION;
    }

    @Override
    public Collection<String> getRequiredSystems() {
        return Arrays.asList( SystemIdentifiers.LOGGING, SystemIdentifiers.CONFIGURATION, SystemIdentifiers.STATISTICS, SystemIdentifiers.RMIJMX,
                SystemIdentifiers.CRON, SystemIdentifiers.RETENTION, AggregatorIdentifiers.CASSANDRA );
    }

    @Override
    public void boot( final E environment ) {
        JSONObject config = environment.getConfiguration().getJSONObject( "aggregator" );
        int poolsize = config.optInt( "poolsize", DEFAULT_POOLSIZE );
        long frequency;
        try {
            frequency = config.getLong( "frequency" );
        } catch ( JSONException e ) {
            frequency = parser.parse( config.getString( "frequency" ) ).longValue();
        }

        aggregator = new Aggregator<E>( environment, poolsize );

        TaskRunner cron = environment.taskRunner();
        cron.runRepeated( aggregator, "Aggregator", Duration.ZERO, Duration.ofSeconds( frequency ), false );
    }

    @Override
    public void shutdown( E environment ) {
        try {
            aggregator.shutdown();
        } catch ( InterruptedException e ) {
            log.warn( "Shutdown Interrupted", e );
        }
    }
}
