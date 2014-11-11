package com.goodgame.profiling.graphite_bifroest.systems.cassandra;

import org.apache.commons.lang3.tuple.Pair;

import com.goodgame.profiling.commons.model.Interval;
import com.goodgame.profiling.commons.model.Metric;
import com.goodgame.profiling.graphite_bifroest.systems.prefixtree.PrefixTree;

public interface CassandraAccessLayer {

    Iterable<String> loadMetricNames();
    Pair<PrefixTree, Integer> loadMostRecentTimestamps();

    Iterable<Metric> loadMetrics( String name, Interval interval );

}
