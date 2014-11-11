package com.goodgame.profiling.graphite_bifroest.systems.cassandra.wrapper;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import com.goodgame.profiling.commons.model.Interval;
import com.goodgame.profiling.commons.model.Metric;
import com.goodgame.profiling.graphite_bifroest.systems.cassandra.CassandraDatabase;
import com.goodgame.profiling.graphite_retentions.RetentionLevel;
import com.goodgame.profiling.graphite_retentions.RetentionTable;

public abstract class MetricIterator implements Iterator<Metric> {

    protected Metric nextMetric = null;

    @Override
    public boolean hasNext() {
        return ( nextMetric != null );
    }

    @Override
    public Metric next() {
        if ( nextMetric == null ) {
            throw new NoSuchElementException();
        }
        Metric result = nextMetric;
        nextMetric = seekNext();
        return result;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    protected abstract Metric seekNext();

    protected Metric peekNext() {
        return nextMetric;
    }

    protected Metric nextFrom( long timestamp ) {
        while ( hasNext() ) {
            Metric metric = next();
            if ( metric.timestamp() <= timestamp ) {
                return metric;
            }
        }
        return null;
    }

    public static MetricIterator create( CassandraDatabase database, String name, Interval interval,
            Iterator<Entry<RetentionLevel, List<RetentionTable>>> levels ) {
        if ( !levels.hasNext() ) {
            return new MetricIterator() {

                @Override
                protected Metric seekNext() {
                    return null;
                }

            };
        } else {
            Entry<RetentionLevel, List<RetentionTable>> entry = levels.next();
            BlockIterator blocks = new BlockIterator( database, name, interval, entry.getValue() );
            MetricIterator next = create( database, name, interval, levels );
            return new LeveledMetricIterator( interval, entry.getKey(), blocks, next );
        }
    }

}
