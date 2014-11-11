package com.goodgame.profiling.commons.statistics;

import java.time.Clock;
import java.time.Instant;
import com.goodgame.profiling.commons.statistics.storage.MetricStorage;

public final class WriteToStorageEvent {
    private final Instant when;
    private final MetricStorage storage;

    public WriteToStorageEvent( Clock clock, MetricStorage storage ) {
        this.when = clock.instant();
        this.storage = storage;
    }

    public Instant when() {
        return when;
    }

    public MetricStorage storageToWriteTo() {
        return storage;
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
        return String.format( "WriteToStorageEvent[ when=%s, storage=%s]", when, storage );
    }
}
