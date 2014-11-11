package com.goodgame.profiling.commons.statistics.calllog;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

class CallLogEntry {

	private final long timestampMillis;
	private final String description;
	private final long durationMillis;

	public static final CompositeType ROW_TYPE;
	public static final String[] INDEXES = new String[] { "calltime" };
	static {
		final String[] columns = new String[] { "calltime", "description", "duration" };
		final String[] columnDescriptions = new String[] { "Time of the call", "Something to identify the call with", "How long that call took" };
		final OpenType<?>[] columnTypes = new OpenType[] { SimpleType.DATE, SimpleType.STRING, SimpleType.LONG };

		try {
			ROW_TYPE = new CompositeType( "calllogEntry", "A single entry in the call log", columns, columnDescriptions, columnTypes );
		} catch ( OpenDataException e ) {
			throw new IllegalStateException( "Cannot create row type", e );
		}
	}

	CallLogEntry( long timestampMillis, String description, long durationMillis ) {
		this.timestampMillis = timestampMillis;
		this.description = description;
		this.durationMillis = durationMillis;
	}

	public CompositeData toTableRow() throws OpenDataException {
		Map<String, Object> values = new HashMap<String, Object>();
		values.put( "calltime", new Date( timestampMillis ) );
		values.put( "description", this.description );
		values.put( "duration", durationMillis );

		return new CompositeDataSupport( CallLogEntry.ROW_TYPE, values );
	}

}
