package com.goodgame.profiling.commons.systems.statistics;

import com.goodgame.profiling.commons.statistics.gathering.StatisticGatherer;

public interface EnvironmentWithMutableStatisticsGatherer extends EnvironmentWithStatisticsGatherer {
	void setStatisticGatherer( StatisticGatherer gatherer );
}
