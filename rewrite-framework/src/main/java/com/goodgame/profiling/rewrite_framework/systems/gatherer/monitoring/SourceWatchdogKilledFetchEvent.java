package com.goodgame.profiling.rewrite_framework.systems.gatherer.monitoring;

public final class SourceWatchdogKilledFetchEvent {
    private final String sourceId;

    private SourceWatchdogKilledFetchEvent( String sourceId ) {
        this.sourceId = sourceId;
    }

    public String sourceId() {
        return sourceId;
    }
}
