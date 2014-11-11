package com.goodgame.profiling.rewrite_framework.drain.statistics;

public class DrainMetricOutputEvent {

	private String drainID;
	private int numMetrics;
		
	public DrainMetricOutputEvent(String drainID, int numMetrics){
		this.drainID = drainID;
		this.numMetrics = numMetrics;
	}
		
	public String getDrainID(){
		return drainID;
	}
		
	public int getNumMetrics(){
		return numMetrics;
	}
}
