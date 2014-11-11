package com.goodgame.profiling.rewrite_framework.systems.gatherer.monitoring;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.MetaInfServices;

import com.goodgame.profiling.commons.statistics.eventbus.EventBusManager;
import com.goodgame.profiling.commons.statistics.gathering.StatisticGatherer;

@MetaInfServices
public final class FetchConfigLogger implements StatisticGatherer {
    private static final Logger log = LogManager.getLogger();

    @Override
    public void init() {
        EventBusManager.subscribe( FetchConfigurationLoadedEvent.class, e -> {
            if ( log.isDebugEnabled() ) {
                log.debug( e.fetchConf() );
            }
        } );
    }
}
