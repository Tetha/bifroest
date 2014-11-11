package com.goodgame.profiling.graphite_bifroest.metric_cache;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.StampedLock;

import com.goodgame.profiling.commons.model.Interval;
import com.goodgame.profiling.commons.model.Metric;
import com.goodgame.profiling.graphite_bifroest.systems.cassandra.CassandraAccessLayer;
import com.goodgame.profiling.graphite_retentions.RetentionStrategy;

public final class OneMetricCache {
    private static final int RETRY_COUNT = 5;

    private final CassandraAccessLayer database;

    private final StampedLock lock;

    private final String metricName;
    private final OneMetricCacheBackend backend;

    private final long frequency;

    private boolean alreadyHaveSomeValuesFromGatherer;

    public OneMetricCache( CassandraAccessLayer database, RetentionStrategy strategy, String metricName, OneMetricCacheBackend backend ) {
        this.database = database;
        this.lock = new StampedLock();
        this.metricName = metricName;
        this.backend = backend;

        this.frequency = strategy.levels().get( 0 ).frequency();

        Instant now = Clock.systemUTC().instant();
        Instant queryStart = now.minusSeconds( this.frequency * backend.size() );

        Iterable<Metric> metrics = database.loadMetrics( metricName, new Interval( queryStart.getEpochSecond(), now.getEpochSecond() ) );
        doAddToCache( metrics );

        alreadyHaveSomeValuesFromGatherer = false;
    }

    private int timestampToIndex( long timestamp ) {
        long bucketIndex = timestamp / frequency;
        return (int) bucketIndex;
    }

    private void doAddToCache( Iterable<Metric> metrics ) {
        for( Metric metric : metrics ) {
            this.backend.put( timestampToIndex( metric.timestamp() ), metric.value() );
        }
    }

    private void backfetch() {
        Instant now = Clock.systemUTC().instant();
        long queryStart = this.backend.upperBound() * frequency;

        Iterable<Metric> metrics = database.loadMetrics( metricName, new Interval( queryStart, now.getEpochSecond() ) );
        doAddToCache( metrics );
    }

    public void addToCache( Collection<Metric> metrics ) {
        long stamp = lock.writeLock();
        if ( !alreadyHaveSomeValuesFromGatherer ) {
            backfetch();
        }
        doAddToCache( metrics );
        alreadyHaveSomeValuesFromGatherer = true;
        lock.unlockWrite( stamp );
    }

    private Optional<List<Metric>> doGetValues( Interval interval ) {
        List<Metric> ret = new ArrayList<>();
        // Return one element BEFORE the requested interval if possible
        // That way, the client knows whether he should additionally hit the database.
        int startOfSearch = Math.max( timestampToIndex( interval.start() - 1 ), backend.lowerBound() );
        int endOfSearch = Math.min( timestampToIndex( interval.end() + 1 ), backend.upperBound() );
        for( int index = startOfSearch; index < endOfSearch; index++ ) {
            double potentialValue = backend.get( index );
            if ( !Double.isNaN( potentialValue ) ) {
                ret.add( new Metric( metricName, index * frequency, potentialValue ) );
            }
        }

        return Optional.of( ret );
    }

    public Optional<List<Metric>> getValues( Interval interval ) {
        for( int i = 0; i < RETRY_COUNT; i++ ) {
            long stamp = lock.tryOptimisticRead();

            Optional<List<Metric>> ret = doGetValues( interval );

            if ( lock.validate( stamp ) ) {
                return ret;
            }
        }

        long stamp = lock.readLock();

        Optional<List<Metric>> ret = doGetValues( interval );

        lock.unlockRead( stamp );

        return ret;
    }

    /* evil package private method: leak backend, so we can reuse it after this object dies */
    OneMetricCacheBackend getBackend() {
        return backend;
    }
}
