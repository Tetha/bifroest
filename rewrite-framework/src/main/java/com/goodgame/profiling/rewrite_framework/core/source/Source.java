package com.goodgame.profiling.rewrite_framework.core.source;

import com.goodgame.profiling.rewrite_framework.core.source.handler.SourceUnitHandler;
import com.goodgame.profiling.rewrite_framework.core.source.timestamp.Timestamp;

public interface Source<U> {
    String sourceId();
    boolean load( Timestamp timestamp, long now, SourceUnitHandler<U> unitHandler );
}
