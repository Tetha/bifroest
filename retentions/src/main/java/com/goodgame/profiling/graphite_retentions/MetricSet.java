package com.goodgame.profiling.graphite_retentions;

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.commons.collections4.iterators.FilterIterator;

import com.goodgame.profiling.commons.model.Interval;
import com.goodgame.profiling.commons.model.Metric;

/**
 * A set of metrics which
 * 
 * @author sglimm
 *
 */
public class MetricSet extends AbstractSet<Metric> {
    private final String name;
    private final long startTimestamp;
    private final long step;
    private final double[] values;

    public MetricSet( String name, Interval interval, long step ) {
        if ( interval.start() % step != 0 ) {
            throw new IllegalArgumentException( String.format(
                    "start(%d) must divide the length of interval(%d)",
                    step,
                    interval.start()
                    ) );
        }
        if ( interval.end() % step != 0 ) {
            throw new IllegalArgumentException( String.format(
                    "end(%d) must divide the length of interval(%d)",
                    step,
                    interval.end()
                    ) );
        }
        if ( ( interval.end() - interval.start() ) / step > Integer.MAX_VALUE ) {
            throw new IndexOutOfBoundsException( String.format(
                    "requested interval is too large! (%d)",
                    ( interval.end() - interval.start() ) / step > Integer.MAX_VALUE
                    ) );
        }

        this.name = name;
        this.startTimestamp = interval.start();
        this.step = step;
        this.values = new double[(int) ( ( interval.end() - interval.start() ) / step )];
        for( int i = 0; i < this.values.length; i++ ) {
            this.values[i] = Double.NaN;
        }
    }

    public void setValue( int index, double value ) {
        this.values[index] = value;
    }

    public double[] values() {
        return values;
    }

    @Override
    public Iterator<Metric> iterator() {
        return new FilterIterator<>(
                new Iterator<Metric>() {
                    private int nextIndex;

                    @Override
                    public boolean hasNext() {
                        return nextIndex < values.length;
                    }

                    @Override
                    public Metric next() {
                        Metric ret = new Metric( name, startTimestamp + nextIndex * step, values[nextIndex] );
                        nextIndex++;
                        return ret;
                    }
                }, metric -> !Double.isNaN( metric.value() ) );
    }

    @Override
    public int size() {
        // The cast to int is safe, because values is an array, which
        // cannot be larger than Integer.MAX_VALUE
        return (int) Arrays.stream( values ).filter( value -> !Double.isNaN( value ) ).count();
    }
}
