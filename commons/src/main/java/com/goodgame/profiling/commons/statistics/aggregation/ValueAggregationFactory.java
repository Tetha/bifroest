package com.goodgame.profiling.commons.statistics.aggregation;

public interface ValueAggregationFactory { 
    String getFunctionName();
    ValueAggregation createAggregation();
}
