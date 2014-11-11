package com.goodgame.profiling.commons.statistics.aggregation;

public final class SumAggregation implements ValueAggregation {
    private double sum = 0.0;

    @Override
    public void consumeValue( double value ) {
        sum += value;
    }

    @Override
    public double getAggregatedValue() {
        return sum;
    }

    @Override
    public void reset() {
        sum = 0.0;
    }

    @Override
    public double aggregateDirectly( double... values ) {
        double value = 0;
        for( int i = 0; i < values.length; i++ ) {
            value += values[i];
        }
        return value;
    }
}
