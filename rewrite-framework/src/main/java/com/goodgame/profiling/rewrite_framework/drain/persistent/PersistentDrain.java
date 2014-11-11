package com.goodgame.profiling.rewrite_framework.drain.persistent;

import java.io.IOException;
import java.util.Collection;

import com.goodgame.profiling.commons.model.Metric;

public interface PersistentDrain {

    void output( Collection<Metric> metrics ) throws IOException;

    void shutdown();

    void dumpInfos();
}
