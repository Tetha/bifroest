package com.goodgame.profiling.graphite_aggregator.systems.cassandra.statistics;

import com.goodgame.profiling.graphite_retentions.RetentionTable;

// May be fired multiple times, because createTableIfNecessary doesn't know if if actually created a table.
public class CreateTableEvent {

    private final long timestamp;
    private final RetentionTable table;

    public CreateTableEvent( long timestamp, RetentionTable table ) {
        this.timestamp = timestamp;
        this.table = table;
    }

    public long timestamp() {
        return timestamp;
    }

    public RetentionTable table() {
        return table;
    }

}
