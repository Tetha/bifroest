package com.goodgame.profiling.commons.statistics.aggregation;

import org.kohsuke.MetaInfServices;

@MetaInfServices
public final class MinAggregationFactory implements ValueAggregationFactory {
    @Override
    public String getFunctionName() {
            return "min";
    }

    @Override
    public ValueAggregation createAggregation() {
        return new MinAggregation();
    }
}
