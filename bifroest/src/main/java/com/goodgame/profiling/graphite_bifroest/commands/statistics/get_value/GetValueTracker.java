package com.goodgame.profiling.graphite_bifroest.commands.statistics.get_value;

import org.kohsuke.MetaInfServices;

import com.goodgame.profiling.commons.statistics.SimpleProgramStateTracker;
import com.goodgame.profiling.commons.statistics.gathering.StatisticGatherer;

@MetaInfServices
public final class GetValueTracker implements StatisticGatherer {
    public static final String GET_VALUE_STAGES = "commands.get_value.stages";
    @Override
    public void init() {
        SimpleProgramStateTracker.forContext( GET_VALUE_STAGES )
                                 .storingIn( "commandExecution", "get_values", "stage-timing" )
                                 .build();
    }
}
