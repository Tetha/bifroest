package com.goodgame.profiling.rewrite_framework.systems.gatherer.monitoring;

import org.kohsuke.MetaInfServices;

import com.goodgame.profiling.commons.statistics.WriteToStorageEvent;
import com.goodgame.profiling.commons.statistics.eventbus.EventBusManager;
import com.goodgame.profiling.commons.statistics.gathering.StatisticGatherer;

@MetaInfServices
public class SourceWatchdogMonitor implements StatisticGatherer {
    private int count;

    public void init() {
        EventBusManager.subscribe( SourceWatchdogKilledFetchEvent.class, e -> {
            count++;
        });

        EventBusManager.subscribe( WriteToStorageEvent.class, e -> {
            e.storageToWriteTo().getSubStorageCalled( "SourceWatchdog" ).store( "killedSources", count );
        });
    }
}
