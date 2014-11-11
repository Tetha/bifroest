package com.goodgame.profiling.rewrite_framework.drain;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.goodgame.profiling.commons.model.Metric;
import com.goodgame.profiling.rewrite_framework.core.drain.Drain;

public class StubDrain implements Drain {
    private boolean flushRemainingBuffersCalled = false;
    private boolean closeCalled = false;
    private List<Metric> metricsOutputted = new LinkedList<>();

    @Override
    public synchronized void flushRemainingBuffers() throws IOException {
        flushRemainingBuffersCalled = true;
    }
    
    @Override
    public synchronized void close() throws IOException {
        closeCalled = true;
    }

    @Override
    public synchronized void output(List<Metric> metrics) throws IOException {
        metricsOutputted.addAll(metrics);
    }

    public synchronized boolean isFlushRemainingBuffersCalled() {
        return flushRemainingBuffersCalled;
    }
    
    public synchronized boolean isCloseCalled() {
        return closeCalled;
    }

    public synchronized List<Metric> getMetricsOutputted() {
        return metricsOutputted;
    }
}
