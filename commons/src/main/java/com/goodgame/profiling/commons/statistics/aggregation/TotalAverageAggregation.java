package com.goodgame.profiling.commons.statistics.aggregation;

public final class TotalAverageAggregation implements ValueAggregation {
    private double sum = 0.0;
    private int count = 0;

    @Override
    public void consumeValue( double value ) {
        sum += value;
        count++;
    }

    @Override
    public double getAggregatedValue() {
        return count == 0 ? 0 : sum / count;
    }

    @Override
    public void reset() {
        count = 0;
        sum = 0.0;
    }

    @Override
    public double aggregateDirectly( double... values ) {
        double value = 0;
        for( int i = 0; i < values.length; i++ ) {
            value += values[i];
        }
        return value / values.length;
    }
}
