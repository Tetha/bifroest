package com.goodgame.profiling.graphite_bifroest.commands;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.goodgame.profiling.commons.model.Interval;
import com.goodgame.profiling.commons.model.Metric;
import com.goodgame.profiling.commons.statistics.aggregation.LastAggregation;
import com.goodgame.profiling.commons.statistics.aggregation.ValueAggregation;
import com.goodgame.profiling.graphite_bifroest.metric_cache.EnvironmentWithMetricCache;
import com.goodgame.profiling.graphite_bifroest.metric_cache.MetricCache;
import com.goodgame.profiling.graphite_bifroest.systems.cassandra.CassandraAccessLayer;
import com.goodgame.profiling.graphite_bifroest.systems.cassandra.EnvironmentWithCassandra;
import com.goodgame.profiling.graphite_retentions.Aggregator;
import com.goodgame.profiling.graphite_retentions.RetentionConfiguration;
import com.goodgame.profiling.graphite_retentions.RetentionLevel;
import com.goodgame.profiling.graphite_retentions.RetentionStrategy;
import com.goodgame.profiling.graphite_retentions.bootloader.EnvironmentWithRetentionStrategy;

public final class GetValueCommandTest {

    private static final String NAME = "metric name";

    private interface TestEnvironment extends EnvironmentWithCassandra, EnvironmentWithRetentionStrategy, EnvironmentWithMetricCache {
    }

    @Mock
    private TestEnvironment environment;

    @Mock
    private CassandraAccessLayer database;

    @Mock
    private RetentionConfiguration retentions;

    @Mock
    private MetricCache cache;

    @Before
    public void createMocks() {
        MockitoAnnotations.initMocks( this );

        RetentionLevel levelA = new RetentionLevel( "lvlA", 10, 5, 100 );
        RetentionLevel levelB = new RetentionLevel( "lvlB", 100, 1, 1000 );
        RetentionStrategy strategy = new RetentionStrategy( "foo", 3, Arrays.asList( levelA, levelB ), 10, 15, 10 );

        when( environment.cassandraAccessLayer() ).thenReturn( database );
        when( environment.retentions() ).thenReturn( retentions );
        when( environment.metricCache() ).thenReturn( cache );

        when( cache.getValues( any(), any() ) ).thenReturn( Optional.empty() );

        when( retentions.findStrategyForMetric( anyString() ) ).thenReturn( strategy );
        when( retentions.findFunctionForMetric( anyString() ) ).thenAnswer( new Answer<ValueAggregation>() {

            @Override
            public ValueAggregation answer( InvocationOnMock invocation ) throws Throwable {
                return new LastAggregation();
            }

        } );
    }

    @Test
    public void testCommandFromJSON() {
        assertEquals( "get_values", new GetValueCommand<TestEnvironment>().getJSONCommand() );
    }

    private JSONObject makeRequest( long start, long end ) {
        JSONObject input = new JSONObject();
        input.put( "command", "get_values" );
        input.put( "name", NAME );
        input.put( "startTimestamp", start );
        input.put( "endTimestamp", end );
        return input;
    }

    private JSONObject makeResult( long start, long end, long step, double... values ) {
        JSONObject result = new JSONObject();
        JSONObject timeDefinition = new JSONObject();
        timeDefinition.put( "start", start );
        timeDefinition.put( "end", end );
        timeDefinition.put( "step", step );
        result.put( "time_def", timeDefinition );

        JSONArray valueArray = new JSONArray();
        for ( int i = 0; i < values.length; i++ ) {
            valueArray.put( values[i] );
        }

        return result.put( "values", valueArray );
    }

    @Test( expected = IllegalArgumentException.class )
    public void testNegativeStartTimestamp() {
        JSONObject input = makeRequest( -5000, 6000 );
        new GetValueCommand<TestEnvironment>().execute( input, environment );
    }

    @Test( expected = IllegalArgumentException.class )
    public void testNegativeEndTimestamp() {
        JSONObject input = makeRequest( 5000, -6000 );
        new GetValueCommand<TestEnvironment>().execute( input, environment );
    }

    @Test( expected = IllegalArgumentException.class )
    public void testUnorderedStartEndTimestamp() {
        JSONObject input = makeRequest( 8000, 6000 );
        new GetValueCommand<TestEnvironment>().execute( input, environment );
    }

    @Test
    public void testFirstLevel() {
        long now = System.currentTimeMillis();
        now = Aggregator.alignTo( now, 10 );
        Interval interval = new Interval( now - 350, now - 300 );

        JSONObject input = makeRequest( interval.start(), interval.end() );
        JSONObject expectedOutput = makeResult( interval.start(), interval.end(), 10, 1, 2, 3, 4, 5 );

        List<Metric> metrics = new ArrayList<>();
        // We expect database metrics in descending order, exclusive end
        metrics.add( new Metric( NAME, now - 310, 5 ) );
        metrics.add( new Metric( NAME, now - 320, 4 ) );
        metrics.add( new Metric( NAME, now - 330, 3 ) );
        metrics.add( new Metric( NAME, now - 340, 2 ) );
        metrics.add( new Metric( NAME, now - 350, 1 ) );
        when( database.loadMetrics( eq( NAME ), any( Interval.class ) ) ).thenReturn( metrics );

        JSONObject output = new GetValueCommand<TestEnvironment>().execute( input, environment );

        assertEquals( expectedOutput.toString(), output.toString() );
        verify( database ).loadMetrics( NAME, interval );
    }

}
