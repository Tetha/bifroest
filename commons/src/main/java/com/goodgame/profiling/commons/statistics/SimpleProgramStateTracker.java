package com.goodgame.profiling.commons.statistics;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

import org.apache.commons.collections4.map.LazyMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.goodgame.profiling.commons.statistics.eventbus.EventBus;
import com.goodgame.profiling.commons.statistics.eventbus.EventBusManager;
import com.goodgame.profiling.commons.statistics.storage.MetricStorage;
import com.goodgame.profiling.commons.util.stopwatch.AsyncClock;
import com.goodgame.profiling.commons.util.stopwatch.StopWatchWithStates;

public class SimpleProgramStateTracker {
    private static final Logger log = LogManager.getLogger();

    // most of these are quasi-final, i.e. they are set once
    private String contextIdentifier;

    private String[] substorageName = new String[0];

    private SimpleProgramStateTracker( String contextIdentifier ) {
        this.contextIdentifier = contextIdentifier;
    }

    public static SimpleProgramStateTracker forContext( String contextIdentifier ) {
        return new SimpleProgramStateTracker( contextIdentifier );
    }

    public SimpleProgramStateTracker storingIn( String... substorageName ) {
        this.substorageName = substorageName;
        return this;
    }

    public void build() {
        AsyncClock clock = new AsyncClock();
        Map<Long, StopWatchWithStates> watches = LazyMap.lazyMap( 
                                       new HashMap<Long, StopWatchWithStates>(), 
                                       () -> new StopWatchWithStates( clock ) );
        EventBus eventBus = EventBusManager.getEventBus();
        log.trace( eventBus );

        eventBus.subscribe( ProgramStateChanged.class, e -> {
            log.trace( "Received ProgramStateChanged in context {} to {}.", contextIdentifier, e.contextIdentifier() );
            if ( e.contextIdentifier().equals( this.contextIdentifier ) ) {
                clock.setInstant( e.when() );
                StopWatchWithStates stopwatch = watches.get( e.threadId() );
                if ( e.nextState().isPresent() ) {
                    stopwatch.startState( e.nextState().get() );
                } else {
                    stopwatch.stop();
                }
            }
        });

        eventBus.subscribe( WriteToStorageEvent.class, e -> {
            clock.setInstant( e.when() );
            MetricStorage destination = e.storageToWriteTo();

            for ( String subStorageNamePart : substorageName ) {
                destination = destination.getSubStorageCalled( subStorageNamePart );
            }

            MetricStorage finalDestination = destination;
            Map<String, LongAdder> stageRuntimes = LazyMap.lazyMap( new HashMap<>(), () -> new LongAdder() );
            watches.forEach( (id, watch) -> {
                watch.consumeStateDurations( (stage, duration) -> {
                    stageRuntimes.get( stage ).add( duration.toNanos() );
                });
            });

            stageRuntimes.forEach( (stage, time) -> {
                finalDestination.store( stage, time.doubleValue() );
            });
        });
    }
}
