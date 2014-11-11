package com.goodgame.profiling.graphite_aggregator.systems.aggregation.statistics;

import java.time.Clock;

import com.goodgame.profiling.commons.statistics.process.ProcessStartedEvent;

public class SingleAggregationSubmitted extends ProcessStartedEvent {
    public SingleAggregationSubmitted( Clock clock ) {
        super( clock );
    }
}
