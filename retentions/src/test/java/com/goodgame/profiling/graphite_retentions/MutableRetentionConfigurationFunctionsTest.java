package com.goodgame.profiling.graphite_retentions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.goodgame.profiling.commons.statistics.aggregation.MaxAggregation;
import com.goodgame.profiling.commons.statistics.aggregation.MinAggregation;
import com.goodgame.profiling.commons.statistics.aggregation.ValueAggregation;

public class MutableRetentionConfigurationFunctionsTest {
    private MutableRetentionConfiguration subject;

    @Before
    public void setUp() {
        subject = new MutableRetentionConfiguration();

        subject.addFunctionEntry( "^abc$", "max" );
        subject.addFunctionEntry( "^def$", "min" );
        subject.addFunctionEntry( "^xyz$", "last" );
    }

    @Test
    public void testSimpleAccess() {
        ValueAggregation agg = subject.findFunctionForMetric( "abc" );
        assertEquals( MaxAggregation.class, agg.getClass() );

        ValueAggregation agg2 = subject.findFunctionForMetric( "def" );
        assertEquals( MinAggregation.class, agg2.getClass() );
    }

    @Test
    public void testMultipleAccessesReturnNewInstance() {
        ValueAggregation agg = subject.findFunctionForMetric( "abc" );
        assertEquals( MaxAggregation.class, agg.getClass() );

        // This should hit the cache - verify that a NEW object is returned.
        ValueAggregation agg2 = subject.findFunctionForMetric( "abc" );
        assertEquals( MaxAggregation.class, agg2.getClass() );
        assertTrue( agg != agg2 );
    }
}
