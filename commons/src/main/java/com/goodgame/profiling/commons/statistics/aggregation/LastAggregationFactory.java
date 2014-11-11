package com.goodgame.profiling.commons.statistics.aggregation;

import org.kohsuke.MetaInfServices;

@MetaInfServices
public final class LastAggregationFactory implements ValueAggregationFactory {
    @Override
    public String getFunctionName() {
            return "last";
    }

    @Override
    public ValueAggregation createAggregation() {
        return new LastAggregation();
    }
}
