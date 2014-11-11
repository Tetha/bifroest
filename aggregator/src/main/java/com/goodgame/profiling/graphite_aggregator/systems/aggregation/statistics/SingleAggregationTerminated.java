package com.goodgame.profiling.graphite_aggregator.systems.aggregation.statistics;

import java.time.Clock;

import com.goodgame.profiling.commons.statistics.process.ProcessFinishedEvent;

public class SingleAggregationTerminated extends ProcessFinishedEvent {
    public SingleAggregationTerminated( Clock clock, boolean success ) {
        super( clock, success );
    }
}
