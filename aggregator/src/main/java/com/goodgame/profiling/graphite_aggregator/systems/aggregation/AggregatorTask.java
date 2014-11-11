package com.goodgame.profiling.graphite_aggregator.systems.aggregation;

import java.time.Clock;
import java.util.Collection;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.datastax.driver.core.exceptions.DriverException;
import com.goodgame.profiling.commons.model.Metric;
import com.goodgame.profiling.commons.statistics.eventbus.EventBusManager;
import com.goodgame.profiling.graphite_aggregator.systems.aggregation.statistics.AggregationEvent;
import com.goodgame.profiling.graphite_aggregator.systems.cassandra.CassandraAccessLayer;
import com.goodgame.profiling.graphite_aggregator.systems.cassandra.EnvironmentWithCassandra;
import com.goodgame.profiling.graphite_retentions.RetentionLevel;
import com.goodgame.profiling.graphite_retentions.RetentionTable;
import com.goodgame.profiling.graphite_retentions.bootloader.EnvironmentWithRetentionStrategy;

public class AggregatorTask< E extends EnvironmentWithCassandra & EnvironmentWithRetentionStrategy > implements Runnable {
    private static final Logger log = LogManager.getLogger();
    private static final Clock clock = Clock.systemUTC();

    private final E environment;
    private final RetentionTable table;
    private final RetentionLevel nextLevel;

    public AggregatorTask( E environment, RetentionTable table, RetentionLevel nextLevel ) {
        this.environment = Objects.requireNonNull( environment );
        this.table = Objects.requireNonNull( table );
        this.nextLevel = nextLevel;
    }

    @Override
    public void run() {
        log.entry( table, nextLevel );

        try {
            if ( nextLevel != null ) {
                for ( String name : environment.cassandraAccessLayer().loadMetricNames( table ) ) {
                    try {
                        handleMetrics( table, name );
                    } catch ( Exception e ) {
                        log.warn( "Exception handling metric " + name + " in table " + table.tableName() );
                    }
                }
            }
            environment.cassandraAccessLayer().dropTable( table );

        } catch ( DriverException e ) {
            log.warn( "A problem with Cassandra occured", e );
        } catch ( Exception e ) {
            log.warn( "A totally unexpected exception occured", e );
        }

        log.exit();
    }

    private void handleMetrics( RetentionTable source, String name ) {
        if ( nextLevel == null ) {
            throw new IllegalStateException( "nextLevel must not be null." );
        }
        if ( nextLevel.blockSize() % nextLevel.frequency() != 0 ) {
            throw new IllegalStateException( String.format(
                    "nextLevel.frequency(%d) does not divide nextLevel.blockSize(%d)",
                    nextLevel.blockSize(),
                    nextLevel.frequency() ) );
        }
        if ( nextLevel.frequency() % table.level().frequency() != 0 ) {
            throw new IllegalStateException( String.format(
                    "nextLevel.frequency(%d) does not divide table.level().frequency(%d)",
                    nextLevel.frequency(),
                    table.level().frequency() ) );
        }

        RetentionTable target = new RetentionTable( table.strategy(), nextLevel, table.getInterval().start() / nextLevel.blockSize() );
        CassandraAccessLayer database = environment.cassandraAccessLayer();
        Collection<Metric> aggregatedMetrics = com.goodgame.profiling.graphite_retentions.Aggregator.aggregate(
                name,
                database.loadUnorderedMetrics( source, name ),
                source.getInterval(),
                nextLevel.frequency(),
                environment.retentions()
        );


        database.createTableIfNecessary( target );
        database.insertMetrics( target, aggregatedMetrics );
        EventBusManager.fire( new AggregationEvent( clock.instant(), name, target, aggregatedMetrics.size() ) );
    }
}
