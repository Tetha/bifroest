package com.goodgame.profiling.graphite_aggregator.systems.aggregation.statistics;

import com.goodgame.profiling.commons.statistics.process.ProcessFinishedEvent;

public class AggregationFinishedEvent extends ProcessFinishedEvent {

    public AggregationFinishedEvent( long timestamp, boolean success ) {
        super( timestamp, success );
    }

}
