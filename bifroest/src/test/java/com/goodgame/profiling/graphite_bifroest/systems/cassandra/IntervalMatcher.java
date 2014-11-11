package com.goodgame.profiling.graphite_bifroest.systems.cassandra;

import org.mockito.ArgumentMatcher;

import com.goodgame.profiling.commons.model.Interval;
import com.goodgame.profiling.commons.model.Metric;

public class IntervalMatcher extends ArgumentMatcher<Interval> {

    long min;
    long max;

    public IntervalMatcher( Metric... metrics ) {
        min = Integer.MAX_VALUE;
        max = Integer.MIN_VALUE;
        for ( int i = 0; i < metrics.length; i++ ) {
            min = Math.min( min, metrics[i].timestamp() );
            max = Math.max( max, metrics[i].timestamp() );
        }
    }

    @Override
    public boolean matches( Object argument ) {
        if ( !( argument instanceof Interval ) ) {
            return false;
        }
        Interval interval = (Interval) argument;
        return interval.start() <= min && interval.end() >= max;
    }

}
