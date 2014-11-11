package com.goodgame.profiling.graphite_aggregator.systems.aggregation.statistics;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.MetaInfServices;

import com.goodgame.profiling.commons.statistics.WriteToStorageEvent;
import com.goodgame.profiling.commons.statistics.eventbus.EventBusManager;
import com.goodgame.profiling.commons.statistics.gathering.StatisticGatherer;
import com.goodgame.profiling.commons.statistics.storage.MetricStorage;
import com.goodgame.profiling.commons.statistics.units.format.DurationFormatter;
import com.goodgame.profiling.commons.statistics.units.format.SiFormatter;
import com.goodgame.profiling.commons.util.stopwatch.AsyncClock;
import com.goodgame.profiling.commons.util.stopwatch.Stopwatch;
import com.goodgame.profiling.graphite_retentions.RetentionTable;

@MetaInfServices
public class AggregationStatusReporter implements StatisticGatherer {
    private static final Logger log = LogManager.getLogger();

    private static final SiFormatter siFormatter = new SiFormatter();
    private static final DurationFormatter durationFormatter = new DurationFormatter();

    private AsyncClock clock = new AsyncClock();
    private Stopwatch total = new Stopwatch( clock );
    private Stopwatch thisAggregation = new Stopwatch( clock );
    private long totalValues;
    private final Map<String, Long> values = new HashMap<>();
    private final Map<RetentionTable, Long> tables = new HashMap<>();
    private long aggregationsSubmitted;
    private long aggregationsTerminated;
    private long aggregationsRemaining;

    @Override
    public void init() {
        EventBusManager.subscribe( AggregationStartedEvent.class, event -> {
            clock.setInstant( event.when() );
            total.start();
            thisAggregation.reset();
            thisAggregation.start();

            log.info( "Aggregation started" );
            aggregationsRemaining = 0;
            tables.clear();
        } );

        EventBusManager.subscribe( AggregationFinishedEvent.class, event -> {
            log.info( "Unfinished Aggregations: " + aggregationsRemaining );

            clock.setInstant( event.when() );
            total.stop();
            thisAggregation.stop();
            Duration duration = thisAggregation.duration();
            if ( event.success() ) {
                log.info( "Aggregation finished - took " + durationFormatter.format( duration ) );
            } else {
                log.info( "Aggregation failed" );
            }
        } );

        EventBusManager.subscribe( AggregationEvent.class, event -> {
            if ( tables.containsKey( event.table() ) ) {
                tables.put( event.table(), tables.get( event.table() ) + event.numValues() );
                log.trace( "Writing to table " + event.table().tableName() );
            } else {
                tables.put( event.table(), (long) event.numValues() );
                // log info only once per table
                log.info( "Writing to table " + event.table().tableName() );
            }
            String levelName = event.table().strategy().name() + "_" + event.table().level().name();
            long value = values.containsKey( levelName ) ? values.get( levelName ) : 0;
            values.put( levelName, value + event.numValues() );
            totalValues += event.numValues();
            log.trace( "Aggregated " + siFormatter.format( event.numValues() ) + " values on level" + levelName + " for metric " + event.metricName() );
        } );

        EventBusManager.subscribe( SingleAggregationSubmitted.class, e -> {
            aggregationsSubmitted++;
            aggregationsRemaining++;
        } );

        EventBusManager.subscribe( SingleAggregationTerminated.class, e -> {
            aggregationsTerminated++;
            aggregationsRemaining--;
        } );

        EventBusManager.subscribe( WriteToStorageEvent.class, e -> {
            MetricStorage storage = e.storageToWriteTo();

            storage.store( "duration", total.duration().toNanos() );
            storage.store( "totalAggregations", totalValues );
            MetricStorage sub = storage.getSubStorageCalled( "aggregations" );
            for ( Entry<String, Long> entry : values.entrySet() ) {
                sub.store( entry.getKey(), entry.getValue() );
            }
            storage.store( "aggregationsSubmitted", aggregationsSubmitted );
            storage.store( "aggregationsTerminated", aggregationsTerminated );
        } );
    }
}
