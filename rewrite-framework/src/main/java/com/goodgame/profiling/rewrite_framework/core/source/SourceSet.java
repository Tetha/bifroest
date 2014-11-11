package com.goodgame.profiling.rewrite_framework.core.source;

import java.time.Duration;
import java.util.List;

import com.goodgame.profiling.rewrite_framework.core.source.timestamp.Timestamp;

public interface SourceSet<U> {
    List<Source<U>> generateSources();
    Timestamp timestamp();
    Duration abortFetchAfter();
}
