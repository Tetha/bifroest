package com.goodgame.profiling.rewrite_framework.core.drain;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import com.goodgame.profiling.commons.model.Metric;

public interface Drain extends Closeable {
    void flushRemainingBuffers() throws IOException;
    void output( List<Metric> metrics ) throws IOException;
}
