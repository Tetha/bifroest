package com.goodgame.profiling.rewrite_framework.drain;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.goodgame.profiling.commons.model.Metric;
import com.goodgame.profiling.commons.statistics.units.SI_PREFIX;
import com.goodgame.profiling.commons.statistics.units.TIME_UNIT;
import com.goodgame.profiling.commons.statistics.units.format.TimeFormatter;
import com.goodgame.profiling.rewrite_framework.core.drain.AbstractWrappingDrain;
import com.goodgame.profiling.rewrite_framework.core.drain.Drain;

public class BufferingDrain extends AbstractWrappingDrain {
    private static final Logger log = LogManager.getLogger();

    private final List<Metric> buffer = new LinkedList<>();
    private final int capacity;
    private final long warnLimitInMillis;

    private long firstMetricReceivedTimestampMillis = Integer.MIN_VALUE;

    private static final TimeFormatter timeFormatter = new TimeFormatter(SI_PREFIX.MILLI, TIME_UNIT.SECOND);

    public BufferingDrain(int capacity, long warnLimitInMillis, Drain inner) {
        super(inner);
        this.capacity = capacity;
        this.warnLimitInMillis = warnLimitInMillis;
    }

    @Override
    public synchronized void flushRemainingBuffers() throws IOException {
        sendAll();
        inner.flushRemainingBuffers();
    }

    @Override
    public synchronized void close() throws IOException {
        if ( !buffer.isEmpty() ) {
            log.warn( "Closing non-empty BufferingDrain!" );
        }
        
        inner.close();
    }

    @Override
    public synchronized void output(List<Metric> metrics) throws IOException {
        for (Metric metric : metrics) {
            if ( buffer.isEmpty() ) {
                assert( firstMetricReceivedTimestampMillis == Integer.MIN_VALUE );
                firstMetricReceivedTimestampMillis = System.currentTimeMillis();
            }
            
            buffer.add(metric);
            optionallyEmptyBuffer();
        }
    }

    private void optionallyEmptyBuffer() throws IOException {
        if (buffer.size() >= capacity) {
            sendAll();
        }
    }

    private void sendAll() throws IOException {
        if (buffer.size() > 0) {
            long nowMillis = System.currentTimeMillis();
            if (nowMillis - firstMetricReceivedTimestampMillis > warnLimitInMillis) {
                log.warn("Metrics have been in buffer for " + timeFormatter.format(nowMillis - firstMetricReceivedTimestampMillis));
            } else {
                log.debug("Metrics have been in buffer for " + timeFormatter.format(nowMillis - firstMetricReceivedTimestampMillis));
            }
        }

        inner.output(buffer);
        buffer.clear();
        firstMetricReceivedTimestampMillis = Integer.MIN_VALUE;
    }
}
