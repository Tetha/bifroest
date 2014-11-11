package com.goodgame.profiling.rewrite_framework.core.source.timestamp;

public class DefaultTimestamp extends Timestamp {
	private static final int DEFAULT_DELTA = 5*60;

	public DefaultTimestamp() {
		super( null, TimestampCreator.getTimestampPriority("default") );
	}

	@Override
	public long getTime( long currentTime, String sourceId ) {
		return currentTime - DEFAULT_DELTA;
	}
}
