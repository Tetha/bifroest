package com.goodgame.profiling.rewrite_framework.drain.statistics;

import java.util.HashMap;
import java.util.Map;

import org.kohsuke.MetaInfServices;

import com.goodgame.profiling.commons.statistics.WriteToStorageEvent;
import com.goodgame.profiling.commons.statistics.eventbus.EventBusManager;
import com.goodgame.profiling.commons.statistics.gathering.StatisticGatherer;
import com.goodgame.profiling.commons.statistics.storage.MetricStorage;

@MetaInfServices
public class DrainStatusReporter implements StatisticGatherer {
    private Map<String, Integer> numMetrics = new HashMap<String, Integer>();

    @Override
    public void init() {
        EventBusManager.subscribe( DrainMetricOutputEvent.class, e -> {
            if ( !numMetrics.containsKey( e.getDrainID() ) ) {
                numMetrics.put( e.getDrainID(), e.getNumMetrics() );
            } else {
                numMetrics.put( e.getDrainID(), numMetrics.get( e.getDrainID() ) + e.getNumMetrics() );
            }
        } );

        EventBusManager.subscribe( WriteToStorageEvent.class, e -> {
            MetricStorage subStorage = e.storageToWriteTo().getSubStorageCalled( "metrics output" );

            for( String drainID : numMetrics.keySet() ) {
                subStorage.store( drainID, numMetrics.get( drainID ) );
            }
        });
    }
}
