package com.goodgame.profiling.commons.statistics.gathering;


/**
 * Classes with this interfaces will be initialized pretty early in the startup
 * so they can register with the event bus and receive information about the
 * gathering runs from there.
 */
public interface StatisticGatherer {

	void init( );

}
