package com.goodgame.profiling.rewrite_framework.drain.statistics;

import java.util.concurrent.atomic.LongAdder;

import org.kohsuke.MetaInfServices;

import com.goodgame.profiling.commons.statistics.WriteToStorageEvent;
import com.goodgame.profiling.commons.statistics.eventbus.EventBusManager;
import com.goodgame.profiling.commons.statistics.gathering.StatisticGatherer;

@MetaInfServices
public class AsyncDrainQueueSizeReporter implements StatisticGatherer {
    LongAdder queueSize = new LongAdder();

    @Override
    public void init() {
        EventBusManager.subscribe( AsyncDrainQueueSizeChangedEvent.class, e -> queueSize.add( e.getDelta() ) );
        EventBusManager.subscribe( WriteToStorageEvent.class,
                e -> e.storageToWriteTo().getSubStorageCalled( "async-drain" ).store( "queue-size", queueSize.longValue() ) );
    }
}
