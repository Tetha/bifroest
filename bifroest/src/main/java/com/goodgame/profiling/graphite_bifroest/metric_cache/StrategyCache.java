package com.goodgame.profiling.graphite_bifroest.metric_cache;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import com.goodgame.profiling.commons.model.Interval;
import com.goodgame.profiling.commons.model.Metric;
import com.goodgame.profiling.commons.statistics.cache.CacheTracker;
import com.goodgame.profiling.commons.statistics.eventbus.EventBusManager;
import com.goodgame.profiling.commons.statistics.jmx.MBeanManager;
import com.goodgame.profiling.graphite_bifroest.metric_cache.statistics.RaceConditionTriggeredEvent;
import com.goodgame.profiling.graphite_bifroest.systems.cassandra.CassandraAccessLayer;
import com.goodgame.profiling.graphite_retentions.RetentionStrategy;

public class StrategyCache implements StrategyCacheMBean {
    private static final Logger log = LogManager.getLogger();
    private static final Marker PARTIAL_HIT_MARKER = MarkerManager.getMarker( "PARTIAL_HIT" );

    private static final Clock clock = Clock.systemUTC();

    private Map<String, OneMetricCache> cache = new ConcurrentHashMap<>();
    private Map<String, Instant> cacheLineEntered = new ConcurrentHashMap<>();

    private final CassandraAccessLayer database;
    private final RetentionStrategy strategy;
    private final CacheStrategy cacheStrategy;
    private final EvictionStrategy evictionStrategy;

    private final BackendPool pool;

    private final String cacheName;

    private CacheTracker tracker;

    public StrategyCache( CassandraAccessLayer database, RetentionStrategy strategy, CacheStrategy cacheStrategy, EvictionStrategy evictionStrategy ) {
        this.database = database;
        this.strategy = strategy;
        this.cacheStrategy = cacheStrategy;
        this.evictionStrategy = evictionStrategy;
        this.cacheName = "MetricCache-" + strategy.name();
        this.pool = new BackendPool( strategy.visibleCacheSize(), strategy.totalCacheSize(), strategy.cacheLineWidth() );
        this.tracker = CacheTracker.storingIn( "Caches", cacheName );

        MBeanManager.registerStandardMBean( this,
                StrategyCache.class.getPackage().getName() + "." + cacheName + ":type=" + StrategyCache.class.getSimpleName(),
                StrategyCacheMBean.class );

    }

    // FIXME: Handle changing retention config
    public Optional<List<Metric>> getValues( String metricName, Interval interval ) {
        if ( !cacheStrategy.shouldICache( metricName, interval ) ) {
            tracker.cacheDodge( cache.size(), strategy.visibleCacheSize() );
            return Optional.empty();
        }

        evictionStrategy.accessing( metricName );

        OneMetricCache cacheline = cache.get( metricName );
        if ( cacheline == null ) {
            // Small race condition here: might create an additional
            // OneMetricCache which will trigger
            // an additional database query - but no more harm done.
            if ( pool.isEmpty() ) {
                evictCacheLine( evictionStrategy.whomShouldIEvict() );
                tracker.cacheEviction();
            }
            Optional<OneMetricCacheBackend> backend = pool.getNextFree();
            if ( backend.isPresent() ) {
                OneMetricCache frontend = new OneMetricCache( database, strategy, metricName, backend.get() );
                pool.notifyFrontendCreated( frontend );
                if ( cache.putIfAbsent( metricName, frontend ) != null ) {
                    EventBusManager.fire( new RaceConditionTriggeredEvent( cacheName ) );
                } else {
                    cacheLineEntered.put( metricName, clock.instant() );
                }
                cacheline = cache.get( metricName );
                tracker.cacheMiss( cache.size(), strategy.visibleCacheSize() );
                return cacheline.getValues( interval );
            } else {
                return Optional.empty();
            }
        } else {
            Optional<List<Metric>> metrics = cacheline.getValues( interval );
            if ( metrics.isPresent() ) {
                if( !metrics.get().isEmpty()
                        && metrics.get().get( 0 ).timestamp() <= interval.start() ) {
                    tracker.cacheHit( cache.size(), strategy.visibleCacheSize() );
                } else {
                    tracker.cachePartialHit( cache.size(), strategy.visibleCacheSize() );
                    log.info( PARTIAL_HIT_MARKER, "got a partial cache hit for metric {}", metricName );
                }
            } else {
                tracker.cacheMiss( cache.size(), strategy.visibleCacheSize() );
            }
            return metrics;
        }
    }

    public void put( Metric metric ) {
        OneMetricCache cacheline = cache.get( metric.name() );
        if ( cacheline != null ) {
            cacheline.addToCache( Arrays.asList( metric ) );
        }
    }

    @Override
    public Map<String, String> getCacheLineAge() {
        Instant now = clock.instant();
        Map<String, String> ret = new HashMap<>();
        cacheLineEntered.forEach( ( metricName, when ) -> ret.put( metricName, Duration.between( when, now ).toString() ) );
        return ret;
    }

    @Override
    public void evictCacheLine( String metricToRemove ) {
        cache.remove( metricToRemove );
        cacheLineEntered.remove( metricToRemove );
    }
}
