package com.goodgame.profiling.commons.systems.net.throttle;

import static org.junit.Assert.assertEquals;

import java.time.Duration;
import java.time.Instant;

import org.junit.Test;

import com.goodgame.profiling.commons.util.stopwatch.AsyncClock;

public class TimeBasedSensorTest {
    private Instant start = Instant.ofEpochSecond( 100 );
    private Instant halfway = Instant.ofEpochSecond( 150 );
    private Instant finish = Instant.ofEpochSecond( 200 );
    private Instant justBeyond = Instant.ofEpochSecond( 201 );
    private Instant wayBeyond = Instant.ofEpochSecond( 250 );

    private AsyncClock clock = new AsyncClock();
    private TimeBasedSensor subject = new TimeBasedSensor( clock, start, Duration.between( start, finish ) );

    @Test
    public void test() {
        clock.setInstant( start );
        assertEquals( 0d, subject.getValue(), 0d );
        clock.setInstant( halfway );
        assertEquals( .5d, subject.getValue(), 0d );
        clock.setInstant( finish );
        assertEquals( 1d, subject.getValue(), 0d );
        clock.setInstant( justBeyond );
        assertEquals( 1d, subject.getValue(), 0d );
        clock.setInstant( wayBeyond );
        assertEquals( 1d, subject.getValue(), 0d );
    }
}
