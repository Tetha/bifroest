package com.goodgame.profiling.commons.util.stopwatch;

import static org.junit.Assert.assertEquals;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import org.junit.Test;

public class StopwatchTest {

    @Test
    public void testOneRun() {
        Instant startTime = Instant.parse( "2014-07-28T13:06:00Z" );
        Instant endTime = startTime.plus( Duration.ofSeconds( 5 ) );
        Instant queryTime = startTime.plus( Duration.ofSeconds( 6 ) );

        Clock clock = mock( Clock.class );
        when(clock.instant()).thenReturn( startTime, endTime, queryTime );

        Stopwatch subject = new Stopwatch( clock );
        subject.start( );
        subject.stop( );

        assertEquals( Duration.ofSeconds( 5 ), subject.duration( ) );
    }

    @Test
    public void testMultipleRuns() {
        Instant startTime = Instant.parse( "2014-07-28T13:06:00Z" );
        Instant endTime = startTime.plus( Duration.ofSeconds( 5 ) );
        Instant startTime2 = startTime.plus( Duration.ofSeconds( 6 ) );
        Instant endTime2 = startTime.plus( Duration.ofSeconds( 10 ) );
        Instant queryTime = startTime.plus( Duration.ofSeconds( 15 ) );

        Clock clock = mock( Clock.class );
        when(clock.instant()).thenReturn( startTime, endTime, startTime2, endTime2, queryTime );

        Stopwatch subject = new Stopwatch( clock );
        subject.start( );
        subject.stop( );
        subject.start( );
        subject.stop( );

        assertEquals( Duration.ofSeconds( 9 ), subject.duration( ) );
    }

    @Test
    public void testQueryDuringSecondRun() {
        Instant startTime = Instant.parse( "2014-07-28T13:06:00Z" );
        Instant endTime = startTime.plus( Duration.ofSeconds( 5 ) );
        Instant startTime2 = startTime.plus( Duration.ofSeconds( 6 ) );
        Instant queryTime = startTime.plus( Duration.ofSeconds( 7 ) );
        Instant endTime2 = startTime.plus( Duration.ofSeconds( 10 ) );
        Instant queryTime2 = startTime.plus( Duration.ofSeconds( 15 ) );

        Clock clock = mock( Clock.class );
        when(clock.instant()).thenReturn( startTime, endTime, startTime2, queryTime, endTime2, queryTime2 );

        Stopwatch subject = new Stopwatch( clock );
        subject.start( );
        subject.stop( );
        subject.start( );

        assertEquals( Duration.ofSeconds( 6 ), subject.duration( ) );

        subject.stop( );

        assertEquals( Duration.ofSeconds( 9 ), subject.duration( ) );
    }
}
