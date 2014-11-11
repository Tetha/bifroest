package com.goodgame.profiling.commons.statistics.aggregation;

public class WindowMaxAggregation implements ValueAggregation {
    private final double[] values;
    private int count;
    private int nextFreeIndex;

    public WindowMaxAggregation( int windowSize ) {
        this.values = new double[windowSize];
        this.nextFreeIndex = 0;
        this.count = 0;
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
        double max = Double.MIN_VALUE;

        for ( int i = 0; i < count; i++ ) {
            if ( values[i] > max ) {
                max = values[i];
            }
        }

        return max;
    }

    @Override
    public void reset() {
        this.nextFreeIndex = 0;
        this.count = 0;
    }

    @Override
    public double aggregateDirectly( double... values ) {
        // Is this method used anywhere???
        // We don't know what window size to use here - WindowAverageAggregation
        // aggregates over all values, so we do the same here.
        WindowMaxAggregation agg = new WindowMaxAggregation( values.length );

        for ( double value : values ) {
            agg.consumeValue( value );
        }

        return agg.getAggregatedValue();
    }
}
