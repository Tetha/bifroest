package com.goodgame.profiling.commons.statistics.duration;

import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.TabularData;

public interface PartitionedDurationStatisticsMBean {
	TabularData getCallLog() throws OpenDataException;
	TabularData getCallCounts() throws OpenDataException;
	TabularData getAverageDurations() throws OpenDataException;
	TabularData getAverageDurationsFromLastCalls() throws OpenDataException;
	TabularData getMaxDurations() throws OpenDataException;
    TabularData getMaxDurationsFromLastCalls() throws OpenDataException;
    
	double getTotalCallCounts();
	double getTotalAverageDuration();
	double getTotalAverageDurationFromLastCalls();
	double getMaxDuration();
    double getMaxDurationFromLastCalls();
}
