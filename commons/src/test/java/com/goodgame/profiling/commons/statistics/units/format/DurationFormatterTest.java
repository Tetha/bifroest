package com.goodgame.profiling.commons.statistics.units.format;

import static org.junit.Assert.*;

import java.time.Duration;

import org.junit.Test;

public class DurationFormatterTest {
    @Test
    public void test() {
        DurationFormatter subject = new DurationFormatter();

        assertEquals("1m 5s", subject.format( Duration.ofSeconds( 65 ) ).trim() );
    }
}
