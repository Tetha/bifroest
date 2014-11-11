package com.goodgame.profiling.graphite_aggregator.systems.aggregation;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.goodgame.profiling.commons.statistics.eventbus.EventBusManager;
import com.goodgame.profiling.commons.systems.configuration.EnvironmentWithJSONConfiguration;
import com.goodgame.profiling.graphite_aggregator.systems.aggregation.statistics.AggregationFinishedEvent;
import com.goodgame.profiling.graphite_aggregator.systems.aggregation.statistics.AggregationStartedEvent;
import com.goodgame.profiling.graphite_aggregator.systems.aggregation.statistics.SingleAggregationSubmitted;
import com.goodgame.profiling.graphite_aggregator.systems.aggregation.statistics.SingleAggregationTerminated;
import com.goodgame.profiling.graphite_aggregator.systems.cassandra.EnvironmentWithCassandra;
import com.goodgame.profiling.graphite_retentions.RetentionLevel;
import com.goodgame.profiling.graphite_retentions.RetentionLevelIterator;
import com.goodgame.profiling.graphite_retentions.RetentionStrategy;
import com.goodgame.profiling.graphite_retentions.RetentionTable;
import com.goodgame.profiling.graphite_retentions.bootloader.EnvironmentWithRetentionStrategy;

public class Aggregator< E extends EnvironmentWithCassandra & EnvironmentWithRetentionStrategy & EnvironmentWithJSONConfiguration > implements Runnable {
    private static final Logger log = LogManager.getLogger();

    private final E environment;
    private final ExecutorService executor;

    boolean running;

    public Aggregator( E environment, int poolsize ) {
        this.environment = environment;
        this.executor = Executors.newFixedThreadPool( poolsize );
    }

    public void shutdown() throws InterruptedException {
        running = false;
        executor.shutdown();
        executor.awaitTermination( Long.MAX_VALUE, TimeUnit.SECONDS );
    }

    @Override
    public void run() {
        running = true;
        try {
            EventBusManager.synchronousFire( new AggregationStartedEvent( System.currentTimeMillis() ) );

            environment.getConfigurationLoader().loadConfiguration();

            for ( RetentionStrategy strategy : environment.retentions().getAllStrategies() ) {
                long now = System.currentTimeMillis() / 1000;
                for ( RetentionLevelIterator it = strategy.levelIterator(); it.isValid(); it.advance()) {
                    handleLevel( strategy, it.sourceLevel(), it.targetLevel(), now );
                    // Set 'now' to the start of the current level
                    now -= now % it.sourceLevel().blockSize();   // Earliest time stamp in head block
                    now += it.sourceLevel().blockSize();         // Latest possible time stamp in head block (is in the future)
                    now -= it.sourceLevel().size();              // Earliest time stamp in earliest block in source level

                    // Check for shutdown
                    if ( !running ) {
                        EventBusManager.synchronousFire( new AggregationFinishedEvent( System.currentTimeMillis(), false ) );
                        return;
                    }
                }
            }

            EventBusManager.synchronousFire( new AggregationFinishedEvent( System.currentTimeMillis(), true ) );
        } catch ( Exception e ) {
            log.warn( "A totally unexpected exception occured", e );
            EventBusManager.synchronousFire( new AggregationFinishedEvent( System.currentTimeMillis(), false ) );
        }
    }

    private void handleLevel( RetentionStrategy strategy, RetentionLevel current, RetentionLevel next, long now ) {
        log.entry( strategy, current, next, now );

        Collection<Future<?>> futures = new ArrayList<>();

        for ( RetentionTable table : environment.cassandraAccessLayer().loadTables() ) {

            // Skip any uninteresting tables
            if ( !strategy.equals( table.strategy() ) ) {
                log.trace( "Skipping " + table + " due to strategy." );
                continue;

            } else if ( !current.equals( table.level() ) ) {
                log.trace( "Skipping " + table + " due to level." );
                continue;

            } else if ( table.block() > current.indexOf( now ) - current.blocks() ) {
                log.trace( "Skipping " + table + " due to time." );
                continue;

            } else {
                log.trace( "Submitting " + table );
                futures.add( executor.submit( new AggregatorTask<E>( environment, table, next ) ) );
                EventBusManager.fire( new SingleAggregationSubmitted( Clock.systemUTC() ) );
            }
        }

        // Wait until all threads are done
        for ( Future<?> future : futures ) {
            try {
                future.get();
                EventBusManager.fire( new SingleAggregationTerminated( Clock.systemUTC(), true ) );
            } catch ( InterruptedException | ExecutionException e ) {
                EventBusManager.fire( new SingleAggregationTerminated( Clock.systemUTC(), false ) );
                log.warn( "Aggregation Interrupted", e );
            }
        }

        log.exit();
    }

}
