package com.goodgame.profiling.graphite_aggregator.systems.aggregation.statistics;

import com.goodgame.profiling.commons.statistics.process.ProcessStartedEvent;

public class AggregationStartedEvent extends ProcessStartedEvent {

    public AggregationStartedEvent( long timestamp ) {
        super( timestamp );
    }

}
