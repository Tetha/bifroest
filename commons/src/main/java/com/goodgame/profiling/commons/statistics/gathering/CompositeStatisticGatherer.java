package com.goodgame.profiling.commons.statistics.gathering;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CompositeStatisticGatherer implements StatisticGatherer {
    private static final Logger log = LogManager.getLogger();

    private List<StatisticGatherer> gatherers;
    
    public CompositeStatisticGatherer() {
        gatherers = new ArrayList<>();
        for ( StatisticGatherer sg : ServiceLoader.load( StatisticGatherer.class ) ) {
            log.info( "StatisticGatherer loaded: " + sg.toString() );
            gatherers.add( sg );
        }
    }

    @Override
    public void init() {
        for ( StatisticGatherer sg : gatherers ) {
            sg.init();
        }
    }
}
