package com.goodgame.profiling.commons.statistics.storage;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.goodgame.profiling.commons.model.Metric;

public class TrieMetricStorage implements MetricStorage {
    private final String prefix;
    private final ConcurrentLinkedQueue<Metric> theList;
    
    public TrieMetricStorage() {
        this( "", new ConcurrentLinkedQueue<>() );
    }

    private TrieMetricStorage( String prefix, ConcurrentLinkedQueue<Metric> theList ) {
        this.prefix = replaceSpaces( prefix );
        this.theList = theList;
    }
    
    private String replaceSpaces( String stringWithSpaces ) {
        return stringWithSpaces.replaceAll( "\\s+", "-" );
    }

    @Override
    public void store( String key, double value ) {
        theList.add( new Metric( prefix + replaceSpaces( key ), System.currentTimeMillis() / 1000, value ) );
    }

    @Override
    public TrieMetricStorage getSubStorageCalled( String subStorageName ) {
        return new TrieMetricStorage( prefix + replaceSpaces( subStorageName ) + ".", theList );
    }

    @Override
    public void finishStoringTheMetrics() throws IOException {
        // Do nothing
    }
    
    public Collection<Metric> getAll() {
        return Collections.unmodifiableCollection( theList );
    }
}
