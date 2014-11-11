package com.goodgame.profiling.graphite_aggregator.systems.cassandra.statistics;

import com.goodgame.profiling.graphite_retentions.RetentionTable;

public class DropTableEvent {

    private final long timestamp;
    private final RetentionTable table;

    public DropTableEvent( long timestamp, RetentionTable table ) {
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
