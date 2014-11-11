package com.goodgame.profiling.commons.statistics.aggregation;

import org.kohsuke.MetaInfServices;

@MetaInfServices
public final class CountAggregationFactory implements ValueAggregationFactory {
    @Override
    public String getFunctionName() {
            return "count";
    }

    @Override
    public ValueAggregation createAggregation() {
        return new CountAggregation();
    }
}
