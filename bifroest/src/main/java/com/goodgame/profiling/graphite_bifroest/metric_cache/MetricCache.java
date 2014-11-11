package com.goodgame.profiling.graphite_bifroest.metric_cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.goodgame.profiling.commons.model.Interval;
import com.goodgame.profiling.commons.model.Metric;
import com.goodgame.profiling.graphite_bifroest.metric_cache.cache_strategy.AlwaysCacheStrategy;
import com.goodgame.profiling.graphite_bifroest.metric_cache.eviction_strategy.NaiveLRUStrategy;
import com.goodgame.profiling.graphite_bifroest.systems.cassandra.CassandraAccessLayer;
import com.goodgame.profiling.graphite_retentions.RetentionConfiguration;
import com.goodgame.profiling.graphite_retentions.RetentionStrategy;

public class MetricCache {
    private static final Logger log = LogManager.getLogger();

    private final Map<String, StrategyCache> caches;
    private final RetentionConfiguration retentionConfig;

    public MetricCache( CassandraAccessLayer database, RetentionConfiguration retentionConfig ) {
        this.caches = new HashMap<>();
        this.retentionConfig = retentionConfig;
        for( RetentionStrategy strategy : retentionConfig.getAllStrategies() ) {
            caches.put( strategy.name(), new StrategyCache( database, strategy, new AlwaysCacheStrategy(), new NaiveLRUStrategy() ) );
            log.info( "Created cache for {}", strategy.name() );
        }
    }

    public Optional<List<Metric>> getValues( String metricName, Interval interval ) {
        RetentionStrategy strategy = retentionConfig.findStrategyForMetric( metricName );
        if( strategy == null ) {
            return Optional.empty();
        } else {
            return caches.get( strategy.name() ).getValues( metricName, interval );
        }
    }

    public void put( Metric metric ) {
        RetentionStrategy strategy = retentionConfig.findStrategyForMetric( metric.name() );
        if( strategy == null ) {
            log.warn( "No strategy found for {}, not caching!", metric.name() );
        } else {
            if( log.isTraceEnabled() ) {
                log.trace( "caches.get( {} ).put( {} )", strategy, metric );
            }
            caches.get( strategy.name() ).put( metric );
        }
    }
}
