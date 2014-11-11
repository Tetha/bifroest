package com.goodgame.profiling.commons.systems.statistics;

import com.goodgame.profiling.commons.boot.interfaces.Environment;
import com.goodgame.profiling.commons.statistics.gathering.StatisticGatherer;

public interface EnvironmentWithStatisticsGatherer extends Environment {
	StatisticGatherer statisticGatherer();
}
