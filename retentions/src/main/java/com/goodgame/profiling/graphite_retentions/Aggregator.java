package com.goodgame.profiling.graphite_retentions;

import java.util.Iterator;

import com.goodgame.profiling.commons.model.Interval;
import com.goodgame.profiling.commons.model.Metric;
import com.goodgame.profiling.commons.statistics.aggregation.ValueAggregation;

public class Aggregator {
    private Aggregator() {
        // Do not instantiate.
    }

    public static long alignTo( long value, long alignTo ) {
        return value - value % alignTo;
    }

    public static Interval alignInterval( Interval interval, long alignTo ) {
        return new Interval(
                alignTo( interval.start(), alignTo ),
                alignTo( interval.end() + alignTo - 1, alignTo ) );
    }

    public static MetricSet aggregate( String name, Iterable<Metric> metrics, Interval interval, long frequency, RetentionConfiguration retentions ) {
    	return aggregate(name, metrics, interval, frequency, retentions, false);
    }

    public static MetricSet aggregate( String name, Iterable<Metric> metrics, Interval interval, long frequency, RetentionConfiguration retentions, boolean removeMetrics ) {
        if ( interval.start() % frequency != 0 ) {
            throw new IllegalArgumentException( String.format(
                    "start(%d) must divide the length of interval(%d)",
                    frequency,
                    interval.start()
                    ) );
        }
        if ( interval.end() % frequency != 0 ) {
            throw new IllegalArgumentException( String.format(
                    "end(%d) must divide the length of interval(%d)",
                    frequency,
                    interval.end()
                    ) );
        }
        if ( ( interval.end() - interval.start() ) / frequency > Integer.MAX_VALUE ) {
            throw new IndexOutOfBoundsException( String.format(
                    "requested interval is too large! (%d)",
                    ( interval.end() - interval.start() ) / frequency > Integer.MAX_VALUE
                    ) );
        }

        long longSize = ( interval.end() - interval.start() ) / frequency;
        if ( longSize > Integer.MAX_VALUE ) {
            throw new IllegalArgumentException( "Requesting more than Integer.MAX_VALUE data points" );
        }
        int size = (int) longSize;

        final ValueAggregation[] aggregations = new ValueAggregation[size];
        final boolean[] valueFound = new boolean[size];

        for( int ii = 0; ii < aggregations.length; ii++ ) {
            aggregations[ii] = retentions.findFunctionForMetric( name );
        }
        
        Iterator<Metric> iter = metrics.iterator();
        while (iter.hasNext()) {
			Metric metric = iter.next();
			if (interval.contains(metric.timestamp())) {
				int index = (int) ((metric.timestamp() - interval.start()) / frequency);
				valueFound[index] = true;
				aggregations[index].consumeValue(metric.value());
				if (removeMetrics) {
					iter.remove();
				}
			}
		}

        MetricSet result = new MetricSet( name, interval, frequency );

        for( int i = 0; i < size; i++ ) {
            if ( valueFound[i] ) {
                result.setValue( i, aggregations[i].getAggregatedValue() );
            }
        }

        return result;
    }
}
