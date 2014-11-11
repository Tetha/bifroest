package com.goodgame.profiling.graphite_bifroest.metric_cache;

import static org.junit.Assert.*;

import org.junit.Test;

import com.goodgame.profiling.graphite_bifroest.metric_cache.OneMetricCacheBackend;

public class OneMetricCacheBackendTest {
    @Test
    public void testAddOne() {
        OneMetricCacheBackend subject = new OneMetricCacheBackend( 10 );
        subject.put( 3, 5 );
        assertEquals( 5, subject.get( 3 ), 0 );
        assertEquals( 4, subject.upperBound() );
    }

    @Test
    public void unsetValuesAreNaN() {
        OneMetricCacheBackend subject = new OneMetricCacheBackend( 10 );
        subject.put( 13, 7 );
        assertTrue( Double.isNaN( subject.get( 3 ) ) );
        assertTrue( Double.isNaN( subject.get( 5 ) ) );
        assertTrue( Double.isNaN( subject.get( 14 ) ) );
    }

    @Test
    public void testRemoveOldValues() {
        OneMetricCacheBackend subject = new OneMetricCacheBackend( 10 );
        subject.put( 1, 1 );
        subject.put( 13, 7 );
        assertTrue( Double.isNaN( subject.get( 1 ) ) );
        assertTrue( Double.isNaN( subject.get( 11 ) ) );
    }

    @Test
    public void testDoesntSetOldValues() {
        OneMetricCacheBackend subject = new OneMetricCacheBackend( 10 );
        subject.put( 13, 7 );
        subject.put( 1, 1 );
        assertTrue( Double.isNaN( subject.get( 1 ) ) );
        assertTrue( Double.isNaN( subject.get( 11 ) ) );
    }

    @Test
    public void testEmptyCache() {
        OneMetricCacheBackend subject = new OneMetricCacheBackend( 10 );
        assertTrue( Double.isNaN( subject.get( 4 ) ) );
    }
}
