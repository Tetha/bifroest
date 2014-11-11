package com.goodgame.profiling.commons.statistics.aggregation;

public interface ValueAggregation {
    /**
     * Add a value to the current aggregation.
     */
    public void consumeValue( double value );

    /**
     * Get the aggregation of all consumed values.
     */
    public double getAggregatedValue();

    /**
     * Clear the internal state.
     */
    public void reset();

    /**
     * Get an aggregated value of an array of values directly, without changing
     * the internal state. This is just a lazy form of accessing the same
     * function in a different way.
     */
    public double aggregateDirectly( double... values );
}
