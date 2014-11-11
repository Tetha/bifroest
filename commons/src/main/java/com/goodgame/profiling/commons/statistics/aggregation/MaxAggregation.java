package com.goodgame.profiling.commons.statistics.aggregation;

public final class MaxAggregation implements ValueAggregation {
    private double currentMax = 0.0;

    @Override
    public void consumeValue( double value ) {
        currentMax = Math.max( value, currentMax );
    }

    @Override
    public double getAggregatedValue() {
        return currentMax;
    }

    @Override
    public void reset() {
        currentMax = 0.0;
    }

    @Override
    public double aggregateDirectly( double... values ) {
        double value = 0;
        for( int i = 0; i < values.length; i++ ) {
            value = Math.max( value, values[i] );
        }
        return value;
    }
}
