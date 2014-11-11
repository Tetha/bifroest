package com.goodgame.profiling.commons.statistics.process;

import java.time.Clock;
import java.time.Instant;

import com.goodgame.profiling.commons.statistics.EventWithInstant;

public abstract class ProcessStartedEvent implements EventWithInstant {
    private final Instant when;

	public ProcessStartedEvent( Clock clock ) {
	    this.when = clock.instant();
	}

    public ProcessStartedEvent( long timestamp ) {
        this.when = Instant.ofEpochMilli( timestamp );
    }

    @Override
    public Instant when() {
        return when;
    }

    public long timestamp() {
        return when.getEpochSecond() * 1000 + when.getNano() / 1000000;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[ when=" + when.toString() + " ]";
    }
}
