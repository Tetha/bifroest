package com.goodgame.profiling.rewrite_framework.core.source.handler.statistics;

import java.time.Clock;
import java.time.Instant;

import com.goodgame.profiling.commons.statistics.EventWithInstant;

public abstract class SourceUnitEvent<U> implements EventWithInstant {
    private final String sourceId;
    private final U unit;
    private final Instant when;

    public SourceUnitEvent( String sourceId, U unit, Clock clock ) {
        this.sourceId = sourceId;
        this.unit = unit;
        this.when = clock.instant();
    }

    public String sourceId() {
        return sourceId;
    }

    public U unit() {
        return unit;
    }

    @Override
    public Instant when() {
        return when;
    }
}
