package com.goodgame.profiling.rewrite_framework.drain.chunking;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.goodgame.profiling.commons.model.Metric;
import com.goodgame.profiling.rewrite_framework.drain.StubDrain;

public class ChunkingDrainTest {
    private StubDrain stub = new StubDrain();
    private ChunkingDrain subject = new ChunkingDrain(stub, 3);

    private List<Metric> metrics = Arrays.asList(
            new Metric("name1", 1, 1),
            new Metric("name2", 2, 2),
            new Metric("name3", 3, 3),
            new Metric("name4", 4, 4),
            new Metric("name5", 5, 5),
            new Metric("name6", 6, 6),
            new Metric("name7", 7, 7)
    );

    @Test
    public void testLessThanOneChunk() throws IOException {
        subject.output( metrics.subList( 0, 2 ) );
        subject.flushRemainingBuffers();
        subject.close();

        assertTrue(stub.isFlushRemainingBuffersCalled());
        assertTrue(stub.isCloseCalled());
        assertEquals( metrics.subList( 0, 2 ), stub.getMetricsOutputted() );
    }

    @Test
    public void testLessExactlyOneChunk() throws IOException {
        subject.output( metrics.subList( 0, 3 ) );
        subject.flushRemainingBuffers();
        subject.close();

        assertTrue(stub.isFlushRemainingBuffersCalled());
        assertTrue(stub.isCloseCalled());
        assertEquals( metrics.subList( 0, 3 ), stub.getMetricsOutputted() );
    }

    @Test
    public void testMoreThanOneChunk() throws IOException {
        subject.output( metrics.subList( 0, 4 ) );
        subject.flushRemainingBuffers();
        subject.close();

        assertTrue(stub.isFlushRemainingBuffersCalled());
        assertTrue(stub.isCloseCalled());
        assertEquals( metrics.subList( 0, 4 ), stub.getMetricsOutputted() );
    }

    @Test
    public void testLessThanTwoChunks() throws IOException {
        subject.output( metrics.subList( 0, 5 ) );
        subject.flushRemainingBuffers();
        subject.close();

        assertTrue(stub.isFlushRemainingBuffersCalled());
        assertTrue(stub.isCloseCalled());
        assertEquals( metrics.subList( 0, 5 ), stub.getMetricsOutputted() );
    }

    @Test
    public void testExactlyTwoChunks() throws IOException {
        subject.output( metrics.subList( 0, 6 ) );
        subject.flushRemainingBuffers();
        subject.close();

        assertTrue(stub.isFlushRemainingBuffersCalled());
        assertTrue(stub.isCloseCalled());
        assertEquals( metrics.subList( 0, 6 ), stub.getMetricsOutputted() );
    }

    @Test
    public void testMoreThanTwoChunks() throws IOException {
        subject.output( metrics.subList( 0, 7 ) );
        subject.flushRemainingBuffers();
        subject.close();

        assertTrue(stub.isFlushRemainingBuffersCalled());
        assertTrue(stub.isCloseCalled());
        assertEquals( metrics.subList( 0, 7 ), stub.getMetricsOutputted() );
    }
}
