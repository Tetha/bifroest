package com.goodgame.profiling.rewrite_framework.drain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import com.goodgame.profiling.commons.model.Metric;

public class AsyncDrainTest {
    private StubDrain stub = new StubDrain();
    private AsyncDrain drain = new AsyncDrain(stub, 1000);
    
    private Metric[] metrics = new Metric[] {
            new Metric("name1", 1, 1),
            new Metric("name2", 2, 2),
            new Metric("name3", 3, 3),
            new Metric("name4", 4, 4),
            new Metric("name5", 5, 5),
            new Metric("name6", 6, 6),
            new Metric("name7", 7, 7)
    };
    
    @Test(timeout = 1000)
    public void testStuff() throws IOException, InterruptedException {
        drain.output(Arrays.asList(metrics[0], metrics[1], metrics[2], metrics[3], metrics[4]));
        
        // Some time for the QueueConsumer to do it's stuff
        while (stub.getMetricsOutputted().size() == 0) {
            Thread.yield();
            // busy wait
        }

        drain.output(Arrays.asList(metrics[5], metrics[6]));
        drain.flushRemainingBuffers();
        drain.close();
        
        List<Metric> expected = new LinkedList<>();
        expected.add(metrics[0]);
        expected.add(metrics[1]);
        expected.add(metrics[2]);
        expected.add(metrics[3]);
        expected.add(metrics[4]);
        expected.add(metrics[5]);
        expected.add(metrics[6]);
        
        assertEquals(expected, stub.getMetricsOutputted());
        assertTrue(stub.isFlushRemainingBuffersCalled());
    }
}
