package com.goodgame.profiling.graphite_bifroest.metric_cache;

import java.util.Map;

public interface StrategyCacheMBean {
    Map<String, String> getCacheLineAge();

    void evictCacheLine( String metricName );
}
