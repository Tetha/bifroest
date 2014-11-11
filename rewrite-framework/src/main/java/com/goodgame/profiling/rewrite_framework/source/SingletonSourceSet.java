package com.goodgame.profiling.rewrite_framework.source;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

import com.goodgame.profiling.rewrite_framework.core.source.Source;
import com.goodgame.profiling.rewrite_framework.core.source.SourceSet;
import com.goodgame.profiling.rewrite_framework.core.source.timestamp.Timestamp;

public final class SingletonSourceSet<U> implements SourceSet<U> {
    private final Source<U> singleSource;
    private final Timestamp timestamp;
    private final Duration abortFetchAfter;

    public SingletonSourceSet( Source<U> source, Timestamp timestamp, Duration abortFetchAfter ) {
        this.singleSource = source;
        this.timestamp = timestamp;
        this.abortFetchAfter = abortFetchAfter;
    }

    @Override
    public List<Source<U>> generateSources() {
        return Collections.singletonList( singleSource );
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
        return "SingletonSourceSet [singleSource=" + singleSource + ", timestamp=" + timestamp + ", abortFetchAfter=" + abortFetchAfter + "]";
    }
}
