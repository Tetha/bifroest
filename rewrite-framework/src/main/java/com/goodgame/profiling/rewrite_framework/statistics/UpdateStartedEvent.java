package com.goodgame.profiling.rewrite_framework.statistics;

import java.time.Clock;

import com.goodgame.profiling.commons.statistics.process.ProcessStartedEvent;

public class UpdateStartedEvent extends ProcessStartedEvent {
    @Deprecated
    public UpdateStartedEvent( long timestamp ) {
        super( timestamp );
    }

    public UpdateStartedEvent( Clock clock ) {
        super( clock );
    }
}
