package com.goodgame.profiling.commons.statistics.units;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.goodgame.profiling.commons.statistics.units.format.TimeFormatter;

public class TimeFormatterTest {

    @Test
    public void testOneDay() {
        TimeFormatter formatter = new TimeFormatter(1, TIME_UNIT.DAY);
        assertEquals("1d ", formatter.format(1));
    }

    @Test
    public void testOneSecond() {
        TimeFormatter formatter = new TimeFormatter(1, TIME_UNIT.SECOND);
        assertEquals("1s ", formatter.format(1));
    }

    @Test
    public void test10ksecondsNoTruncate() {
        TimeFormatter formatter = new TimeFormatter(3, TIME_UNIT.SECOND);
        assertEquals("2h 46m 40s ", formatter.format(10000));
    }

    @Test
    public void test10ksecondsTruncate() {
        TimeFormatter formatter = new TimeFormatter(2, TIME_UNIT.SECOND);
        assertEquals("2h 46m ", formatter.format(10000));
    }

    @Test
    public void testMilliseconds() {
        TimeFormatter formatter = new TimeFormatter(1, TIME_UNIT.SECOND);
        assertThat(formatter.format(.123), is(anyOf(equalTo("123ms "), equalTo("122ms "))));
    }

    @Test
    public void testSecondsAndMilliseconds() {
        TimeFormatter formatter = new TimeFormatter(2, TIME_UNIT.SECOND);
        assertThat(formatter.format(1.23), is(anyOf(equalTo("1s 230ms "), equalTo("1s 229ms "))));
    }

    @Test
    public void testMillisecondsConstructor() {
        TimeFormatter formatter = new TimeFormatter(1, SI_PREFIX.MILLI, TIME_UNIT.SECOND);
        assertThat(formatter.format(123), is(anyOf(equalTo("123ms "), equalTo("122ms "))));
    }

    @Test
    public void testNegatives() {
        TimeFormatter formatter = new TimeFormatter(1, TIME_UNIT.SECOND);
        assertEquals("-1m ", formatter.format(-60));
    }
}
