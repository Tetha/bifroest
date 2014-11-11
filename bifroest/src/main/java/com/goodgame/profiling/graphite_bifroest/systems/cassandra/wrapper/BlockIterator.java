package com.goodgame.profiling.graphite_bifroest.systems.cassandra.wrapper;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.goodgame.profiling.commons.model.Interval;
import com.goodgame.profiling.commons.model.Metric;
import com.goodgame.profiling.graphite_bifroest.systems.cassandra.CassandraDatabase;
import com.goodgame.profiling.graphite_retentions.RetentionTable;

public class BlockIterator implements Iterator<Iterable<Metric>> {

    private final CassandraDatabase database;
    private final String name;
    private final Interval interval;
    private final Iterator<RetentionTable> tables;

    private Iterable<Metric> nextBlock;

    public BlockIterator( CassandraDatabase database, String name, Interval interval, Iterable<RetentionTable> tables ) {
        this.database = database;
        this.name = name;
        this.interval = interval;
        this.tables = tables.iterator();
        nextBlock = seekNext();
    }

    private Iterable<Metric> seekNext() {
        while ( tables.hasNext() ) {
            RetentionTable table = tables.next();
            if ( table.getInterval().intersects( interval ) ) {
                return database.loadMetrics( table, name, interval );
            }
        }
        return null;
    }

    @Override
    public boolean hasNext() {
        return ( nextBlock != null );
    }

    @Override
    public Iterable<Metric> next() {
        if ( nextBlock == null ) {
            throw new NoSuchElementException();
        }
        Iterable<Metric> result = nextBlock;
        nextBlock = seekNext();
        return result;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

}
