package com.goodgame.profiling.commons.statistics.aggregation;

import java.util.Arrays;

public final class WindowAverageAggregation implements ValueAggregation {
    private final double[] values;
    private int count;
    private int nextFreeIndex;

    public WindowAverageAggregation( int valuesToConsider ) {
        values = new double[valuesToConsider];
    }

    @Override
    public void consumeValue( double value ) {
        values[nextFreeIndex] = value;
        if ( count < values.length ) {
            count++;
        }
        nextFreeIndex = ( nextFreeIndex + 1 ) % values.length;
    }

    @Override
    public double getAggregatedValue() {
        if ( count > 0 ) {
            double sum = 0;
            for ( double value : values ) {
                sum += value;
            }
            return sum / count;
        } else {
            return 0;
        }
    }

    @Override
    public void reset() {
        count = 0;
        nextFreeIndex = 0;
        Arrays.fill( values, 0 );
    }

    @Override
    public double aggregateDirectly( double... values ) {
        double value = 0;
        for ( int i = 0; i < values.length; i++ ) {
            value += values[i];
        }
        return value / values.length;
    }
}
