package com.goodgame.profiling.commons.statistics.process;

import static org.junit.Assert.*;

import org.junit.Test;

public class ProcessStartedEventTest {
    private class Dummy extends ProcessStartedEvent{
        public Dummy( long timestamp ) {
            super( timestamp );
        }
    }

    @Test
    public void testBackwardCompatible() {
        ProcessStartedEvent subject = new Dummy(1234567890l);

        assertEquals(1234567890l, subject.timestamp());
    }
}
