package com.goodgame.profiling.rewrite_framework.drain.debug;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.goodgame.profiling.commons.model.Metric;
import com.goodgame.profiling.rewrite_framework.core.drain.AbstractWrappingDrain;
import com.goodgame.profiling.rewrite_framework.core.drain.Drain;

public final class DebugLoggingDrain extends AbstractWrappingDrain {
    private static final Logger log = LogManager.getLogger();

    public DebugLoggingDrain(Drain inner) {
        super(inner);
    }

    @Override
    public void flushRemainingBuffers() throws IOException {
        log.debug("flushing DebugLoggingDrain");
        inner.flushRemainingBuffers();
    }

    @Override
    public void close() throws IOException {
        log.debug("closing DebugLoggingDrain");
        inner.close();
    }

    @Override
    public void output(List<Metric> metrics) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug(StringUtils.join(metrics, "|"));
        }
        inner.output(metrics);
    }
}
