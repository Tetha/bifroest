package com.goodgame.profiling.commons.statistics.storage;

import java.io.IOException;

/**
 * <code>StatisticGatherer</code>s store the collected information in a metric
 * storage after the <code>UpdateFinished</code> event is fired.
 * 
 * This interface deliberately doesn't expose read-access so we can be flexible
 * about the actual storage. This allows us to implement this with a file on the
 * file system, or by directly sending the metrics to graphite.
 */
public interface MetricStorage {

	void store( String key, double value );

	MetricStorage getSubStorageCalled( String subStorageName );

	void finishStoringTheMetrics() throws IOException;

}
