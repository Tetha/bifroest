package com.goodgame.profiling.rewrite_framework.systems.gatherer.monitoring;

import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import com.goodgame.profiling.commons.statistics.EventWithInstant;

public final class QueryAbortFetchEvent implements EventWithInstant {
    private final Instant when;

    private List<String> sourceIdsToAbort;

    public QueryAbortFetchEvent( Clock clock ) {
        when = clock.instant();
    }

    public void setSourceIdsToAbort( List<String> sourceIdsToAbort ) {
        this.sourceIdsToAbort = Collections.unmodifiableList( sourceIdsToAbort );
    }

    public List<String> sourceIdsToAbort() {
        return sourceIdsToAbort;
    }

    public Instant when() {
        return when;
    }
}
