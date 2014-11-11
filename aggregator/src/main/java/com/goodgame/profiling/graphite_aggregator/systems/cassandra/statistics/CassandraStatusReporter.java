package com.goodgame.profiling.graphite_aggregator.systems.cassandra.statistics;

import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.MetaInfServices;

import com.goodgame.profiling.commons.statistics.WriteToStorageEvent;
import com.goodgame.profiling.commons.statistics.eventbus.EventBusManager;
import com.goodgame.profiling.commons.statistics.gathering.StatisticGatherer;
import com.goodgame.profiling.commons.statistics.storage.MetricStorage;
import com.goodgame.profiling.graphite_retentions.RetentionTable;

@MetaInfServices
public class CassandraStatusReporter implements StatisticGatherer {

    private static final Logger log = LogManager.getLogger();

    private Set<RetentionTable> tablesCreated = new HashSet<>();

    private volatile long dropped = 0;
    private volatile long created = 0;

    @Override
    public void init() {
        EventBusManager.subscribe( CreateTableEvent.class, event -> {
            if ( tablesCreated.contains( event.table() ) ) {
                log.debug( "Got duplicate CreatedTableEvent for {}", event.table().toString() );
            } else {
                created += 1;
                log.info( "===============================================================" );
                log.info( "Created table:" + event.table().toString() );
                log.info( "===============================================================" );
            }
        } );

        EventBusManager.subscribe( DropTableEvent.class, event -> {
            dropped += 1;
            log.info( "===============================================================" );
            log.info( "Dropped table:" + event.table().toString() );
            log.info( "===============================================================" );
        } );

        EventBusManager.subscribe( WriteToStorageEvent.class, e -> {
            MetricStorage storage = e.storageToWriteTo();

            storage.store( "createdTables", created );
            storage.store( "droppedTables", dropped );
        } );
    }
}
