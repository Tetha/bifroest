package com.goodgame.profiling.rewrite_framework.source;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

import com.goodgame.profiling.rewrite_framework.core.source.Source;
import com.goodgame.profiling.rewrite_framework.core.source.SourceSet;
import com.goodgame.profiling.rewrite_framework.core.source.timestamp.Timestamp;

public final class SourceSetFromList<U> implements SourceSet<U> {
    private final List<Source<U>> sources;
    private final Timestamp timestamp;
    private final Duration abortFetchAfter;

    public SourceSetFromList( List<Source<U>> sources, Timestamp timestamp, Duration abortFetchAfter ) {
        this.sources = sources;
        this.timestamp = timestamp;
        this.abortFetchAfter = abortFetchAfter;
    }

    @Override
    public List<Source<U>> generateSources() {
        return Collections.unmodifiableList( sources );
    }

    @Override
    public Timestamp timestamp() {
        return timestamp;
    }

    @Override
    public Duration abortFetchAfter() {
        return abortFetchAfter;
    }

    @Override
    public String toString() {
        return "SourceSetFromList [sources=" + sources + ", timestamp=" + timestamp + ", abortFetchAfter=" + abortFetchAfter + "]";
    }
}
