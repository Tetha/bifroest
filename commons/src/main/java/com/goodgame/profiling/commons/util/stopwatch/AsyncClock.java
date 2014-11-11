package com.goodgame.profiling.commons.util.stopwatch;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

public class AsyncClock extends Clock {
    private Instant instant;

    @Override
    public ZoneId getZone() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Clock withZone( ZoneId zone ) {
        throw new UnsupportedOperationException();
    }

    public void setInstant( Instant instant ) {
        this.instant = instant;
    }

    @Override
    public Instant instant() {
        return instant;
    }
}
