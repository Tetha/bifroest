package com.goodgame.profiling.commons.statistics.aggregation;

import org.kohsuke.MetaInfServices;

@MetaInfServices
public final class MaxAggregationFactory implements ValueAggregationFactory {
    @Override
    public String getFunctionName() {
            return "max";
    }
    
    @Override
    public ValueAggregation createAggregation() {
        return new MaxAggregation();
    }
}
