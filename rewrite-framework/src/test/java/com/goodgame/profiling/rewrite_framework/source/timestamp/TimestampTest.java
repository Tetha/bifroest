package com.goodgame.profiling.rewrite_framework.source.timestamp;

import static org.junit.Assert.assertEquals;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.goodgame.profiling.rewrite_framework.core.source.timestamp.DefaultTimestamp;
import com.goodgame.profiling.rewrite_framework.core.source.timestamp.Timestamp;
import com.goodgame.profiling.rewrite_framework.source.timestamp.limit.LimitTimestampFactory;
import com.goodgame.profiling.rewrite_framework.source.timestamp.override.OverrideTimestampFactory;

public class TimestampTest {

    private static final long DEFAULT_DELTA = 5 * 60;

    private Timestamp timestamp;
    private long current;

    @Before
    public void createFromConfig() {

        JSONObject overrides = new JSONObject();
        overrides.put( "test01", 0 );
        overrides.put( "test02", 123456 );

        JSONObject limits = new JSONObject();
        limits.put( "test02", 400 );
        limits.put( "test03", 200 );
        limits.put( "test04", 400 );

        JSONObject json = new JSONObject();
        json.put( "overrides", overrides );
        json.put( "limits", limits );
        json.put( "warn-threshold", "15m" );

        timestamp = new DefaultTimestamp();
        timestamp = new OverrideTimestampFactory().create( timestamp, json );
        timestamp = new LimitTimestampFactory().create( timestamp, json );

        current = 10000000;
    }

    @Test
    public void testOverrideSimple() {
        assertEquals( 0, timestamp.getTime( current, "test01" ) );
    }

    @Test
    public void testOverrideOrdered() {
        assertEquals( 123456, timestamp.getTime( current, "test02" ) );
    }

    @Test
    public void testLimit() {
        assertEquals( current - 200, timestamp.getTime( current, "test03" ) );
    }

    @Test
    public void testDefaultOrdered() {
        assertEquals( current - DEFAULT_DELTA, timestamp.getTime( current, "test04" ) );
    }

    @Test
    public void testDefault() {
        assertEquals( current - DEFAULT_DELTA, timestamp.getTime( current, "test05" ) );
    }

}
