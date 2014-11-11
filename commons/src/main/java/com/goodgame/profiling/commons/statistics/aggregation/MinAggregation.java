package com.goodgame.profiling.commons.statistics.aggregation;

public final class MinAggregation implements ValueAggregation {
    private double currentMin = Double.MAX_VALUE;

    @Override
    public void consumeValue( double value ) {
        currentMin = Math.min( value, currentMin );
    }

    @Override
    public double getAggregatedValue() {
        return currentMin;
    }

    @Override
    public void reset() {
        currentMin = Double.MAX_VALUE;
    }

    @Override
    public double aggregateDirectly( double... values ) {
        double value = Double.MAX_VALUE;
        for( int i = 0; i < values.length; i++ ) {
            value = Math.min( value, values[i] );
        }
        return value;
    }
}
