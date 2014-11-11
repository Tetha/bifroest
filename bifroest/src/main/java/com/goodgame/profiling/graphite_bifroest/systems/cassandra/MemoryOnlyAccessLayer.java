package com.goodgame.profiling.graphite_bifroest.systems.cassandra;

import java.util.Collections;

import org.apache.commons.lang3.tuple.Pair;

import org.json.JSONObject;


import com.goodgame.profiling.commons.model.Interval;
import com.goodgame.profiling.commons.model.Metric;
import com.goodgame.profiling.graphite_bifroest.systems.prefixtree.PrefixTree;


public final class MemoryOnlyAccessLayer implements CassandraAccessLayer {
    @Override
    public Iterable<String> loadMetricNames() {
        return Collections.emptyList();
    }

    @Override
    public Pair<PrefixTree, Integer> loadMostRecentTimestamps() {
        return Pair.of( PrefixTree.fromJSONObject( new JSONObject() ), 0);
    }

    @Override
    public Iterable<Metric> loadMetrics( String name, Interval interval ) {
        return Collections.emptyList();
    }
}
