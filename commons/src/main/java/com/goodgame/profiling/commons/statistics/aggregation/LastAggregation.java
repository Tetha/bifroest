package com.goodgame.profiling.commons.statistics.aggregation;

public final class LastAggregation implements ValueAggregation {
    private double lastValue = 0;

    @Override
    public void consumeValue( double value ) {
        lastValue = value;
    }

    @Override
    public double getAggregatedValue() {
        return lastValue;
    }

    @Override
    public void reset() {
        lastValue = 0;
    }

    @Override
    public double aggregateDirectly( double... values ) {
        if ( values.length <= 0 ) {
            return 0;
        } else {
            return values[values.length - 1];
        }
    }
}
