package com.goodgame.profiling.graphite_bifroest.commands;

import java.time.Clock;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.LongAdder;

import org.apache.commons.collections4.iterators.FilterIterator;
import org.apache.commons.collections4.iterators.IteratorChain;
import org.apache.commons.collections4.iterators.IteratorIterable;
import org.apache.commons.collections4.map.LazyMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.kohsuke.MetaInfServices;

import com.goodgame.profiling.commons.model.Interval;
import com.goodgame.profiling.commons.model.Metric;
import com.goodgame.profiling.commons.statistics.WriteToStorageEvent;
import com.goodgame.profiling.commons.statistics.eventbus.EventBusManager;
import com.goodgame.profiling.commons.statistics.storage.MetricStorage;
import com.goodgame.profiling.commons.systems.net.jsonserver.Command;
import com.goodgame.profiling.commons.util.stopwatch.StopWatchWithStates;
import com.goodgame.profiling.graphite_bifroest.metric_cache.EnvironmentWithMetricCache;
import com.goodgame.profiling.graphite_bifroest.systems.cassandra.EnvironmentWithCassandra;
import com.goodgame.profiling.graphite_retentions.Aggregator;
import com.goodgame.profiling.graphite_retentions.MetricSet;
import com.goodgame.profiling.graphite_retentions.RetentionLevel;
import com.goodgame.profiling.graphite_retentions.RetentionStrategy;
import com.goodgame.profiling.graphite_retentions.bootloader.EnvironmentWithRetentionStrategy;

@MetaInfServices
public final class GetValueCommand< E extends EnvironmentWithCassandra & EnvironmentWithRetentionStrategy & EnvironmentWithMetricCache > implements Command<E> {
    public static final String COMMAND = "get_values";

    private static final Logger log = LogManager.getLogger();

    // tid -> watch
    private ConcurrentMap<Long, StopWatchWithStates> watches = new ConcurrentHashMap<Long, StopWatchWithStates>();

    public GetValueCommand() {
        EventBusManager.subscribe( WriteToStorageEvent.class, e -> {
            MetricStorage destination = e.storageToWriteTo().getSubStorageCalled( "commandExecution" )
                                                            .getSubStorageCalled( "get_values" )
                                                            .getSubStorageCalled( "stage-timing" );

            Map<String, LongAdder> stageRuntimes = LazyMap.lazyMap( new HashMap<>(), () -> new LongAdder() );
            watches.forEach( (id, watch) -> {
                synchronized (watch) {
                    watch.consumeStateDurations( (stage, duration) -> {
                        stageRuntimes.get( stage ).add( duration.toNanos() );
                    });
                }
            });

            stageRuntimes.forEach( (stage, time) -> {
                destination.store( stage, time.doubleValue() );
            });
        });
    }

    @Override
    public List<Class<? super E>> getRequiredEnvironments() {
        return Arrays.<Class<? super E>> asList( EnvironmentWithCassandra.class, EnvironmentWithRetentionStrategy.class, EnvironmentWithMetricCache.class );
    }

    @Override
    public String getJSONCommand() {
        return COMMAND;
    }

    @Override
    public List<Pair<String, Boolean>> getParameters() {
        Pair<String, Boolean> paramName = new ImmutablePair<>( "name", true );
        Pair<String, Boolean> paramStart = new ImmutablePair<>( "startTimestamp", true );
        Pair<String, Boolean> paramEnd = new ImmutablePair<>( "endTimestamp", true );
        return Arrays.asList( paramName, paramStart, paramEnd );
    }

    private void startNextState( String state ) {
        long tid = Thread.currentThread().getId();
        StopWatchWithStates watch = watches.get( tid );
        if ( watch == null ) {
            StopWatchWithStates newWatch = new StopWatchWithStates( Clock.systemUTC() ); 
            StopWatchWithStates oldWatch = watches.putIfAbsent( tid, newWatch );
            watch = oldWatch == null ? newWatch : oldWatch;
        }
        synchronized( watch ) {
            watch.startState( state );
        }

    }

    private void stopCurrentState() {
        long tid = Thread.currentThread().getId();
        StopWatchWithStates watch = watches.get( tid );
        synchronized( watch ) {
            watch.stop();
        }
    }

    @Override
    public JSONObject execute( JSONObject input, E environment ) {
        startNextState( "retention considerations" );
        final String name = input.getString( "name" );

        Interval interval = getIntervalFromJSON( input );
        final RetentionStrategy strategy = environment.retentions().findStrategyForMetric( name );
        final long step = findHighestFrequency( interval.start(), strategy.levels() );
        interval = Aggregator.alignInterval( interval, step );

        startNextState( "loadaggregate" );
        Iterable<Metric> metrics = getMetrics( environment, name, interval );
        MetricSet metricValues = Aggregator.aggregate( name, metrics, interval, step, environment.retentions() );
        startNextState( "json creation" );

        JSONObject result = new JSONObject();
        result.put( "time_def", makeTimespec( interval, step ) );
        result.put( "values", makeValues( metricValues.values() ) );
        stopCurrentState();
        return result;
    }

    private Iterable<Metric> getMetrics( E environment, String name, Interval interval ) {
        return new Iterable<Metric>() {
            @Override
            public Iterator<Metric> iterator() {
                return new FilterIterator<>( _getMetrics( environment, name, interval ).iterator(), metric -> interval.contains( metric.timestamp() ) );
            }
        };
    }

    private Iterable<Metric> _getMetrics( E environment, String name, Interval interval ) {
        log.debug( "Performing fetch for metric " + name + " for " + interval.toString() );
        Optional<List<Metric>> metricsFromCache = environment.metricCache().getValues( name, interval );

        if( metricsFromCache.isPresent() ) {
            if( !metricsFromCache.get().isEmpty()
                    && metricsFromCache.get().get( 0 ).timestamp() <= interval.start() ) {
                log.debug( "Got all results we want from cache." );
                return metricsFromCache.get();
            } else {
                log.debug( "Got some results from cache, but need to get the rest from the database" );
                long refetchEnd = metricsFromCache.get().isEmpty() ? interval.end() : metricsFromCache.get().get( 0 ).timestamp();
                return new IteratorIterable<>( new IteratorChain<>(
                        environment.cassandraAccessLayer().loadMetrics( name, new Interval( interval.start(), refetchEnd ) ).iterator(),
                        metricsFromCache.get().iterator()
                        ) );
            }
        } else {
            log.debug( "Got no results from cache, need to hit the database." );
            return environment.cassandraAccessLayer().loadMetrics( name, interval );
        }
    }

    private static long findHighestFrequency( long timestamp, Iterable<RetentionLevel> levels ) {
        long frequency = 0;
        long now = System.currentTimeMillis() / 1000;
        for ( RetentionLevel level : levels ) {
            frequency = level.frequency();
            now = now - ( now % level.blockSize() ); // Normalize
            now = now + level.blockSize() - level.size(); // Start of level
            if ( timestamp > now ) {
                break;
            }
        }
        return frequency;
    }

    private static Interval getIntervalFromJSON( JSONObject json ) {
        long start = json.getLong( "startTimestamp" );
        long end = json.getLong( "endTimestamp" );

        if ( start < 0 ) {
            throw new IllegalArgumentException( "Negative start timestamp: " + start );
        } else if ( end < 0 ) {
            throw new IllegalArgumentException( "Negative end timestamp: " + start );
        } else if ( end < start ) {
            log.warn( "start < end: " + start + " < " + end );
            throw new IllegalArgumentException( "start < end: " + start + " < " + end );
        }

        return new Interval( start, end );
    }

    private static JSONObject makeTimespec( Interval interval, long frequency ) {
        JSONObject timespec = new JSONObject();
        timespec.put( "start", interval.start() );
        timespec.put( "end", interval.end() );
        timespec.put( "step", frequency );
        return timespec;
    }

    private static JSONArray makeValues( double[] metricValues ) {
        JSONArray values = new JSONArray();
        for ( int i = 0; i < metricValues.length; i++ ) {
            if ( Double.isNaN( metricValues[i] ) ) {
                values.put( i, JSONObject.NULL );
            } else {
                values.put( i, metricValues[i] );
            }
        }

        if ( log.isTraceEnabled() ) {
            int valuesFoundTrue = 0;
            for ( int ii = 0; ii < metricValues.length; ii++ ) {
                if ( Double.isNaN( metricValues[ii] ) ) {
                    valuesFoundTrue++;
                }
            }
            log.trace( "Found " + valuesFoundTrue + " values" );
        }
        return values;
    }

}
