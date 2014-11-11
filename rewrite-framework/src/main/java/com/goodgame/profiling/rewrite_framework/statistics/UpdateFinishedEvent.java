package com.goodgame.profiling.rewrite_framework.statistics;

import java.time.Clock;

import com.goodgame.profiling.commons.statistics.process.ProcessFinishedEvent;

public class UpdateFinishedEvent extends ProcessFinishedEvent {
    @Deprecated
    public UpdateFinishedEvent( long timestamp, boolean success ) {
        super( timestamp, success );
    }

    public UpdateFinishedEvent( Clock clock, boolean success ) {
        super( clock, success );
    }
}
