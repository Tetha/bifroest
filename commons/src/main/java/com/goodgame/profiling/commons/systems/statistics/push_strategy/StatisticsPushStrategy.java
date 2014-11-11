package com.goodgame.profiling.commons.systems.statistics.push_strategy;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;

import com.goodgame.profiling.commons.model.Metric;

public interface StatisticsPushStrategy extends Closeable {
    void pushAll( Collection<Metric> metrics ) throws IOException;
}
