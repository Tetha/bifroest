package com.goodgame.profiling.graphite_bifroest.metric_cache;

public interface EvictionStrategy {
    void accessing( String metricName );
    String whomShouldIEvict();
}
