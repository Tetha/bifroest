package com.goodgame.profiling.commons.statistics.aggregation;

import org.kohsuke.MetaInfServices;

@MetaInfServices
public final class TotalAverageAggregationFactory implements ValueAggregationFactory {
    @Override
    public String getFunctionName() {
            return "average";
    }

    @Override
    public ValueAggregation createAggregation() {
        return new TotalAverageAggregation();
    }
}
