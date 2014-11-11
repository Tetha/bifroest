package com.goodgame.profiling.graphite_bifroest.systems.rebuilder.statistics;

import java.time.Clock;

import com.goodgame.profiling.commons.statistics.process.ProcessFinishedEvent;

public class RebuildFinishedEvent extends ProcessFinishedEvent {
	
	private int numberOfInsertedMetrics;

	public RebuildFinishedEvent( Clock clock, int numberOfInsertedMetrics) {
		super( clock, true);
		
		this.numberOfInsertedMetrics = numberOfInsertedMetrics;
	}
	
	public int numberOfInsertedMetrics(){

		return numberOfInsertedMetrics;
	}
	
}
