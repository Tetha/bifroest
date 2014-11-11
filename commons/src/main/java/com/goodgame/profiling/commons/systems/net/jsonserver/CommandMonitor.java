package com.goodgame.profiling.commons.systems.net.jsonserver;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.goodgame.profiling.commons.statistics.eventbus.EventBusManager;
import com.goodgame.profiling.commons.statistics.units.TIME_UNIT;
import com.goodgame.profiling.commons.statistics.units.format.TimeFormatter;
import com.goodgame.profiling.commons.systems.net.jsonserver.statistics.CommandFinishedEvent;
import com.goodgame.profiling.commons.systems.net.jsonserver.statistics.CommandStartedEvent;

public class CommandMonitor implements Runnable {
    private static final Logger log = LogManager.getLogger();
    private static final TimeFormatter formatter = new TimeFormatter( 2, TIME_UNIT.SECOND );

    private final String interfaceName;
    private final long warnLimit;

    // ThreadID -> ( Timestamp, Command )
    private final Map<Long, Pair<Long, String>> startTimes;

    public CommandMonitor( String interfaceName, long warnLimit ) {
        this.interfaceName = interfaceName;
        this.warnLimit = warnLimit;
        this.startTimes = new ConcurrentHashMap<>();
        init();
    }

    private void init() {
        EventBusManager.subscribe( CommandStartedEvent.class, event -> {
            Objects.requireNonNull( event );
            Objects.requireNonNull( event.interfaceName() );
            if ( event.interfaceName().equals( interfaceName ) ) {
                startTimes.put( event.threadId(), new ImmutablePair<>( System.currentTimeMillis() / 1000, event.command() ) );
            }
        } );
        EventBusManager.subscribe( CommandFinishedEvent.class, event -> {
            if ( event.interfaceName().equals( interfaceName ) ) {
                startTimes.remove( event.threadId() );
            }
        } );
    }

    @Override
    public void run() {
        long now = System.currentTimeMillis() / 1000;
        for ( Entry<Long, Pair<Long, String>> entry : startTimes.entrySet() ) {
            long time = now - entry.getValue().getLeft();
            String command = entry.getValue().getRight();
            if ( time > warnLimit ) {
                log.warn( "Command " + command + " on thread " + entry.getKey() + " is now running " + formatter.format( time ) );
            }
        }
    }
}
