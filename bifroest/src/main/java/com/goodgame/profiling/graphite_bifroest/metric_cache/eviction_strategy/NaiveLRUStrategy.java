package com.goodgame.profiling.graphite_bifroest.metric_cache.eviction_strategy;

import org.apache.commons.collections4.map.LRUMap;

import com.goodgame.profiling.graphite_bifroest.metric_cache.EvictionStrategy;

public class NaiveLRUStrategy implements EvictionStrategy {
    private static final Object THE_OBJECT = new Object();
    private LRUMap<String, Object> decider = new LRUMap<String, Object>( 100 );

    @Override
    public synchronized void accessing( String metricName ) {
        decider.put( metricName, THE_OBJECT );
    }

    @Override
    public synchronized String whomShouldIEvict() {
        String ret = decider.firstKey();
        decider.remove( ret );
        return ret;
    }
}
