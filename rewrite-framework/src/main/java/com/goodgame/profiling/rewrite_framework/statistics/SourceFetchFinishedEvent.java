package com.goodgame.profiling.rewrite_framework.statistics;

import com.goodgame.profiling.commons.statistics.process.ProcessFinishedEvent;

public class SourceFetchFinishedEvent extends ProcessFinishedEvent {

	private final String sourceId;

	public SourceFetchFinishedEvent( String sourceId, long timestamp, boolean success ) {
		super( timestamp, success );
		this.sourceId = sourceId;
	}

	public String sourceId() {
		return sourceId;
	}

}
