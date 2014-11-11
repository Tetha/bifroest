package com.goodgame.profiling.rewrite_framework.systems.gatherer.monitoring;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.goodgame.profiling.commons.statistics.eventbus.EventBusManager;
import com.goodgame.profiling.rewrite_framework.core.source.handler.statistics.SourceUnitEvent;
import com.goodgame.profiling.rewrite_framework.statistics.SourceFetchFinishedEvent;
import com.goodgame.profiling.rewrite_framework.statistics.SourceFetchStartedEvent;
import com.goodgame.profiling.rewrite_framework.statistics.UpdateFinishedEvent;

public final class SourceWatchdog {
    private static final Logger log = LogManager.getLogger();

    private final Map<String, Instant> lastLifeSignOfRunningFetch = new HashMap<>();

    private Map<String, Duration> abortAfterForSourceId;

    public SourceWatchdog( ) {
        EventBusManager.subscribe( SourceFetchStartedEvent.class, e -> {
            lastLifeSignOfRunningFetch.put( e.sourceId(), e.when() );
        });

        EventBusManager.subscribe( SourceUnitEvent.class, e -> {
            lastLifeSignOfRunningFetch.put( e.sourceId(), e.when() );
        });

        EventBusManager.subscribe( SourceFetchFinishedEvent.class, e -> {
            lastLifeSignOfRunningFetch.remove( e.sourceId() );
        });

        EventBusManager.subscribe( UpdateFinishedEvent.class, e -> {
            lastLifeSignOfRunningFetch.clear();
        });

        EventBusManager.subscribe( QueryAbortFetchEvent.class, e -> {
            List<String> toAbort = lastLifeSignOfRunningFetch
                    .entrySet()
                    .stream()
                    .filter( entry -> Duration.between( entry.getValue(), e.when() ).compareTo( abortAfterForSourceId.get( entry.getKey() ) ) > 0 )
                    .map( entry -> entry.getKey() )
                    .collect( Collectors.toList() );
            e.setSourceIdsToAbort( toAbort );
        });
    }

    public void setAbortAfterForSourceId( Map<String, Duration> abortAfterForSourceId ) {
        this.abortAfterForSourceId = abortAfterForSourceId;
    }

    public final static class SourceWatchdogRunnable implements Runnable {
        private static final Clock clock = Clock.systemUTC();

        private final Map<String, Future<Object>> sourceIdToFuture;

        public SourceWatchdogRunnable( Map<String, Future<Object>> sourceIdToFuture ) {
            this.sourceIdToFuture = sourceIdToFuture;
        }

        @Override
        public void run() {
            QueryAbortFetchEvent e = new QueryAbortFetchEvent( clock );
            EventBusManager.synchronousFire( e );
            e.sourceIdsToAbort().forEach( sourceId -> {
                sourceIdToFuture.get( sourceId ).cancel( true ) ;
                log.warn( "Cancelled fetch for {}", sourceId );
            } );
        }
    }
}
