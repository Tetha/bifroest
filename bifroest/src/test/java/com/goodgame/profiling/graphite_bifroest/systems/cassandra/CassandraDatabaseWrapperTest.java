package com.goodgame.profiling.graphite_bifroest.systems.cassandra;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.goodgame.profiling.commons.model.Interval;
import com.goodgame.profiling.commons.model.Metric;
import com.goodgame.profiling.graphite_bifroest.systems.cassandra.wrapper.CassandraDatabaseWrapper;
import com.goodgame.profiling.graphite_retentions.RetentionConfiguration;
import com.goodgame.profiling.graphite_retentions.RetentionLevel;
import com.goodgame.profiling.graphite_retentions.RetentionStrategy;
import com.goodgame.profiling.graphite_retentions.RetentionTable;

public class CassandraDatabaseWrapperTest {

    @Mock
    CassandraDatabase database;

    @Mock
    RetentionConfiguration retentionConfig;

    CassandraAccessLayer cassandra;

    RetentionLevel precise, hourly;
    RetentionStrategy defaultStrategy;
    RetentionTable lvl1blk1, lvl1blk3, dangling, lvl2blk0, lvl2blk1, lvl2blk2;
    Metric metric01, metric02, metric03, metric04, metric05, metric06, metric07, metric08, metric09, metric10;

    @Before
    public void createMocks() {
        MockitoAnnotations.initMocks( this );
        long timestamp = System.currentTimeMillis() / 1000;

        cassandra = new CassandraDatabaseWrapper( database, retentionConfig );

        // Levels and strategy
        precise = new RetentionLevel( "precise", 300, 3, 60 * 60 );
        hourly = new RetentionLevel( "hourly", 60 * 60, 4, 60 * 60 * 24 * 7 );
        defaultStrategy = new RetentionStrategy( "default", 3, Arrays.asList( precise, hourly ), 0, 1, 1 );

        // Head block
        long index = precise.indexOf( timestamp );
        lvl1blk1 = new RetentionTable( defaultStrategy, precise, index );
        // One is missing
        lvl1blk3 = new RetentionTable( defaultStrategy, precise, index - 2 );
        // Dangling block
        dangling = new RetentionTable( defaultStrategy, precise, index - 300 );
        // Random misplaced block - should be ignored
        index = hourly.indexOf( timestamp );
        lvl2blk0 = new RetentionTable( defaultStrategy, hourly, index );
        // Second level head block
        index = hourly.indexOf( lvl1blk3.getInterval().start() );
        lvl2blk1 = new RetentionTable( defaultStrategy, hourly, index );
        // Second level regular block
        lvl2blk2 = new RetentionTable( defaultStrategy, hourly, index - 1 );

        when( retentionConfig.getStrategyForName( defaultStrategy.name() ) ).thenReturn( defaultStrategy );
        when( retentionConfig.findStrategyForMetric( anyString() ) ).thenReturn( defaultStrategy );

        when( database.loadMetricNames( lvl1blk1 ) ).thenReturn( Arrays.asList( "name01", "name02", "name05" ) );
        when( database.loadMetricNames( lvl1blk3 ) ).thenReturn( Arrays.asList( "name03", "name02" ) );
        when( database.loadMetricNames( dangling ) ).thenReturn( Arrays.asList( "name01", "name04", "name05", "name06" ) );
        when( database.loadMetricNames( lvl2blk1 ) ).thenReturn( Arrays.asList( "name05", "name03", "name05", "name06" ) );
        when( database.loadMetricNames( lvl2blk2 ) ).thenReturn( Collections.<String> emptyList() );

        metric01 = new Metric( "name02", lvl1blk1.getInterval().end() - 500, 7 );
        metric02 = new Metric( "name02", lvl1blk1.getInterval().end() - 800, 42 );
        metric03 = new Metric( "name02", lvl1blk3.getInterval().end() - 500, 13 );
        metric04 = new Metric( "name03", lvl1blk3.getInterval().end() - 800, 1 );
        metric05 = new Metric( "name03", lvl2blk2.getInterval().end() - 500, 2 );
        // testLoadMetricAcrossLevelsWithGabs()
        metric06 = new Metric( "name05", lvl1blk1.getInterval().end() - 500, 51 );
        metric07 = new Metric( "name05", dangling.getInterval().start() + 1, 53 );
        metric08 = new Metric( "name05", lvl2blk1.getInterval().start() + 1, 52 );
        // testLoadMetricAcrossLevelsWithFirstMetricIsInLevelTwo()
        metric09 = new Metric( "name06", dangling.getInterval().start() + 1, 61 );
        metric10 = new Metric( "name06", lvl2blk1.getInterval().end() - 50, 62 );

        when( database.loadMetrics( any( RetentionTable.class ), any( String.class ), any( Interval.class ) ) ).thenReturn( Collections.<Metric> emptyList() );

        // Mocks
        List<RetentionTable> list = Arrays.asList( lvl1blk1, lvl1blk3, dangling, lvl2blk0, lvl2blk1, lvl2blk2 );
        Collections.shuffle( list );
        when( database.loadTables() ).thenReturn( list );
    }

    private void initLoadMetrics( String name, RetentionTable table, Metric... metrics ) {
        when( database.loadMetrics( eq( table ), eq( name ), argThat( new IntervalMatcher( metrics ) ) ) ).thenReturn( Arrays.asList( metrics ) );
    }

    private List<Metric> loadMetrics( String name, RetentionTable... tables ) {
        long start = Long.MAX_VALUE, end = Long.MIN_VALUE;
        for ( int i = 0; i < tables.length; i++ ) {
            Interval interval = tables[i].getInterval();
            start = Math.min( start, interval.start() );
            end = Math.max( end, interval.end() );
        }
        Iterable<Metric> metrics = cassandra.loadMetrics( name, new Interval( start, end ) );
        List<Metric> result = new ArrayList<>();
        for ( Metric metric : metrics ) {
            result.add( metric );
        }
        return result;
    }

    @Test
    public void testLoadMetricNames() {
        List<String> names = new ArrayList<>();
        for ( String name : cassandra.loadMetricNames() ) {
            names.add( name );
        }
        assertTrue( names.contains( "name01" ) );
        assertTrue( names.contains( "name02" ) );
        assertTrue( names.contains( "name03" ) );
        assertTrue( names.contains( "name05" ) );
        assertFalse( names.contains( "name06" ) );
        assertFalse( names.contains( "name04" ) );
    }

    @Test
    public void testLoadMetricsInOneTable() {
        initLoadMetrics( "name02", lvl1blk1, metric01, metric02 );
        List<Metric> metrics = loadMetrics( "name02", lvl1blk1 );
        assertTrue( metrics.contains( metric01 ) );
        assertTrue( metrics.contains( metric02 ) );
        assertFalse( metrics.contains( metric03 ) );
    }

    @Test
    public void testLoadMetricsInOneLevel() {
        initLoadMetrics( "name02", lvl1blk1, metric01, metric02 );
        initLoadMetrics( "name02", lvl1blk3, metric03 );
        List<Metric> metrics = loadMetrics( "name02", lvl1blk1, lvl1blk3 );
        assertTrue( metrics.contains( metric01 ) );
        assertTrue( metrics.contains( metric02 ) );
        assertTrue( metrics.contains( metric03 ) );
    }

    @Test
    public void testLoadMetricsAcrossLevels() {
        initLoadMetrics( "name03", lvl1blk3, metric04 );
        initLoadMetrics( "name03", lvl2blk2, metric05 );
        List<Metric> metrics = loadMetrics( "name03", lvl1blk3, lvl2blk2 );
        assertTrue( metrics.contains( metric04 ) );
        assertTrue( metrics.contains( metric05 ) );
    }

    @Test
    public void testLoadMetricAcrossLevelsWithGaps() {
        initLoadMetrics( "name05", lvl1blk1, metric06 );
        initLoadMetrics( "name05", dangling, metric07 );
        initLoadMetrics( "name05", lvl2blk1, metric08 );
        List<Metric> metrics = loadMetrics( "name05", lvl1blk1, dangling, lvl2blk1 );
        assertTrue( metrics.contains( metric06 ) );
        assertTrue( metrics.contains( metric07 ) );
        assertTrue( metrics.contains( metric08 ) );
    }

    @Test
    public void testLoadMetricAcrossLevelsWithFirstMetricIsInLevelTwo() {
        initLoadMetrics( "name06", dangling, metric09 );
        initLoadMetrics( "name06", lvl2blk1, metric10 );
        List<Metric> metrics = loadMetrics( "name06", dangling, lvl2blk1 );
        assertTrue( metrics.contains( metric09 ) );
        assertTrue( metrics.contains( metric10 ) );
    }

}
