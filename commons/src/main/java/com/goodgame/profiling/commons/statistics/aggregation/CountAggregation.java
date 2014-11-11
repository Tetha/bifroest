package com.goodgame.profiling.commons.statistics.aggregation;

public final class CountAggregation implements ValueAggregation {
    private int count;

    @Override
    public void consumeValue( double value ) {
        count++;
    }

    @Override
    public double getAggregatedValue() {
        return count;
    }

    @Override
    public void reset() {
        count = 0;
    }

    @Override
    public double aggregateDirectly( double... values ) {
        return values.length;
    }
}
