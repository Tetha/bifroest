package com.goodgame.profiling.commons.statistics.calllog;

import java.util.LinkedList;

import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;

public final class CallLog {

	private final LinkedList<CallLogEntry> entries;
	private final int capacity;
	public static final TabularType CALL_LOG_TYPE;

	static {
		try {
			CALL_LOG_TYPE = new TabularType( "calllog", "The log of calls", CallLogEntry.ROW_TYPE, CallLogEntry.INDEXES );
		} catch ( OpenDataException e ) {
			throw new IllegalStateException( "Cannot create table type", e );
		}
	}

	public CallLog( int capacity ) {
		if ( capacity <= 0 ) {
			throw new IllegalArgumentException( "capacity cannot be <= 0" );
		}
		this.capacity = capacity;
		this.entries = new LinkedList<CallLogEntry>();
	}

	public void logCall( long startTimestampMillis, String description, long endTimestampMillis ) {
		entries.addFirst( new CallLogEntry( startTimestampMillis, description, endTimestampMillis - startTimestampMillis ) );
		if ( entries.size() > capacity ) {
			entries.removeLast();
		}
	}

	public TabularData toJmxTable() throws OpenDataException {
		TabularDataSupport result = new TabularDataSupport( CALL_LOG_TYPE );
		for ( CallLogEntry entry : entries ) {
			result.put( entry.toTableRow() );
		}
		return result;
	}

}
