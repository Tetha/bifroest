package com.goodgame.profiling.commons.util.stopwatch;

import java.time.Clock;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import org.apache.commons.collections4.map.LazyMap;
import org.apache.commons.lang3.StringUtils;

public class StopWatchWithStates {
    private final Map<String, Stopwatch> watches;

    private Optional<String> currentState;

    public StopWatchWithStates( Clock clock ) {
        this.watches = LazyMap.<String, Stopwatch>lazyMap( new HashMap<>(), () -> new Stopwatch( clock ) );
        currentState = Optional.empty();
    }

    public void reset() {
        watches.values().forEach( watch -> watch.reset() );
        currentState = Optional.empty();
    }

    /**
     * Starts the given state, stops any other state that was running
     */
    public void startState( String newState ) {
        currentState.ifPresent( oldState -> watches.get( oldState ).stop() );
        watches.get( newState ).start();
        currentState = Optional.of( newState );
    }

    public void stop() {
        currentState.ifPresent( oldState -> watches.get( oldState ).stop() );
        currentState = Optional.empty();
    }

    public void consumeStateDurations( BiConsumer<String, Duration> consumer ) {
        watches.forEach( ( state, watch ) -> consumer.accept( state, watch.duration() ) );
    }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals( Object o ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append( "StopWatchWithStates( states=" );
        result.append( StringUtils.join( watches.keySet(), ", " ) );
        result.append( ")" );
        return result.toString();
    }
}
