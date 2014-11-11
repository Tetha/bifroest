package com.goodgame.profiling.commons.util.stopwatch;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * A stop watch.
 *
 * Has the basic methods you expect from a stop watch (start, stop, reset), and
 * a getter to query how long it has been running.
 *
 * @author sglimm
 */
public class Stopwatch {
    private final Clock clock;

    private Duration totalRuntime;
    private boolean running;
    private Instant lastStartTime;

    public Stopwatch( Clock clock ) {
        this.clock = clock;

        reset();
    }

    public void reset() {
        totalRuntime = Duration.ofSeconds( 0 );
        running = false;
        lastStartTime = Instant.MIN;
    }

    public void start( ) {
        if ( !running ) {
            lastStartTime = Instant.now( clock );
            running = true;
        }
    }

    public void stop( ) {
        if ( running ) {
            Duration thisRunTime = Duration.between( lastStartTime, Instant.now( clock ) );
            totalRuntime = totalRuntime.plus( thisRunTime );
            running = false;
        }
    }

    public Duration duration( ) {
        if ( running ) {
            Duration thisRunTime = Duration.between( lastStartTime, Instant.now( clock ) );
            return totalRuntime.plus( thisRunTime );
        } else {
            return totalRuntime;
        }
    }
}
