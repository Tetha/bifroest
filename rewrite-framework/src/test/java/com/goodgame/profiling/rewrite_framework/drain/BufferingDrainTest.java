package com.goodgame.profiling.rewrite_framework.drain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import com.goodgame.profiling.commons.model.Metric;

public class BufferingDrainTest {
    private StubDrain stub = new StubDrain();
    private BufferingDrain drain = new BufferingDrain(3, 5000, stub);
    
    private Metric[] metrics = new Metric[] {
            new Metric("name1", 1, 1),
            new Metric("name2", 2, 2),
            new Metric("name3", 3, 3),
            new Metric("name4", 4, 4),
            new Metric("name5", 5, 5),
            new Metric("name6", 6, 6),
            new Metric("name7", 7, 7)
    };
    
    @Test
    public void testNotEnoughForFlush() throws IOException {
        drain.output(Arrays.asList(metrics[0], metrics[1]));
        
        assertTrue(stub.getMetricsOutputted().isEmpty());
        assertFalse(stub.isFlushRemainingBuffersCalled());
    }
    
    @Test
    public void testEnoughForOneFlush() throws IOException {
        drain.output(Arrays.asList(metrics[0], metrics[1], metrics[2], metrics[3]));
        
        List<Metric> expected = new LinkedList<>();
        expected.add(metrics[0]);
        expected.add(metrics[1]);
        expected.add(metrics[2]);
        
        assertEquals(expected, stub.getMetricsOutputted());
        assertFalse(stub.isFlushRemainingBuffersCalled());
    }
    
    @Test
    public void testEnoughForTwoFlushes() throws IOException {
        drain.output(Arrays.asList(metrics[0], metrics[1], metrics[2], metrics[3], metrics[4], metrics[5], metrics[6]));
        
        List<Metric> expected = new LinkedList<>();
        expected.add(metrics[0]);
        expected.add(metrics[1]);
        expected.add(metrics[2]);
        expected.add(metrics[3]);
        expected.add(metrics[4]);
        expected.add(metrics[5]);
        
        assertEquals(expected, stub.getMetricsOutputted());
        assertFalse(stub.isFlushRemainingBuffersCalled());
    }
    
    @Test
    public void testFlushCalled() throws IOException {
        drain.output(Arrays.asList(metrics[0], metrics[1], metrics[2], metrics[3], metrics[4], metrics[5], metrics[6]));
        drain.flushRemainingBuffers();
        
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
