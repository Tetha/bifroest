package com.goodgame.profiling.commons.statistics.process;

import java.time.Clock;
import java.time.Instant;

import com.goodgame.profiling.commons.statistics.EventWithInstant;

public class ProcessFinishedEvent implements EventWithInstant {
    private final Instant when;
    private final boolean success;

    public ProcessFinishedEvent( Clock clock, boolean success ) {
        this.when = clock.instant();
        this.success = success;
    }

    public ProcessFinishedEvent( long timestamp, boolean success ) {
        this.when = Instant.ofEpochMilli( timestamp );
        this.success = success;
    }

    @Override
    public Instant when() {
        return when;
    }

    public long timestamp() {
        return when.toEpochMilli();
    }

	public boolean success() {
		return success;
	}

    @Override
    public String toString() {
        return getClass().getName() + "[ when=" + when.toString() + ", success=" + success + " ]";
    }
}
