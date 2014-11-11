package com.goodgame.profiling.rewrite_framework.drain.statistics;

import java.time.Instant;

public class AsyncDrainQueueSizeChangedEvent {
    private final Instant when;
    private final int delta;

    public AsyncDrainQueueSizeChangedEvent( Instant when, int delta ) {
        this.when = when;
        this.delta = delta;
    }

    public Instant getWhen() {
        return when;
    }

    public int getDelta() {
        return delta;
    }
}
