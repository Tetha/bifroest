package com.goodgame.profiling.commons.systems.net.throttle;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

public final class TimeBasedSensor implements Sensor {
    private final Clock clock;
    private final Instant startup;
    private final Duration durationToMaximum;

    public TimeBasedSensor( Clock clock, Instant startup, Duration durationToMaximum ) {
        this.clock = clock;
        this.startup = startup;
        this.durationToMaximum = durationToMaximum;
    }

    @Override
    public double getValue() {
        Instant now = clock.instant();
        Duration timePassed = Duration.between( startup, now );
        if ( timePassed.compareTo( durationToMaximum ) >= 0 ) {
            return 1;
        } else {
            return (double)timePassed.toNanos() / durationToMaximum.toNanos();
        }
    }

    @Override
    public String toString() {
        return "TimeBasedSensor [clock=" + clock + ", startup=" + startup + ", durationToMaximum=" + durationToMaximum + "]";
    }
}
