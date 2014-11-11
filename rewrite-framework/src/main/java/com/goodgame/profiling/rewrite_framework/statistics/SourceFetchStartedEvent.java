package com.goodgame.profiling.rewrite_framework.statistics;

import com.goodgame.profiling.commons.statistics.process.ProcessStartedEvent;

public class SourceFetchStartedEvent extends ProcessStartedEvent {

	private final String sourceId;
	private final int rewrites;

	public SourceFetchStartedEvent( String sourceId, int rewrites, long timestamp ) {
		super( timestamp );
		this.sourceId = sourceId;
		this.rewrites = rewrites;
	}

	public String sourceId() {
		return sourceId;
	}

	public int numRewrites() {
		return rewrites;
	}

}
