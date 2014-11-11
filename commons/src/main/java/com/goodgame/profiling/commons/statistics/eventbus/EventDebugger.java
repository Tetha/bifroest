package com.goodgame.profiling.commons.statistics.eventbus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.kohsuke.MetaInfServices;

import com.goodgame.profiling.commons.statistics.gathering.StatisticGatherer;

@MetaInfServices
public class EventDebugger implements StatisticGatherer {

    private static final Logger log = LogManager.getLogger();

    private static final Marker EVENT_MARKER = MarkerManager.getMarker( "EVENT_MARKER" );

    @Override
    public void init() {
        // The event debugger puts some stress on our EventBus thread.
        // Only register it, if event debugging is actually enabled.
        // Because of that, to use event debugging, you have to restart the service.
        if ( log.isTraceEnabled() ) {
            log.warn( "Registering event debugger" );
            EventBusManager.subscribe( Object.class, e -> {
                log.trace( EVENT_MARKER, e.toString() );
            } );
        } else {
            log.info( "NOT registering event debugger" );
        }
    }
}
