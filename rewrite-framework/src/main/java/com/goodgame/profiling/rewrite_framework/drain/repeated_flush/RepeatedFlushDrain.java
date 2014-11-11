package com.goodgame.profiling.rewrite_framework.drain.repeated_flush;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.goodgame.profiling.commons.model.Metric;
import com.goodgame.profiling.commons.systems.cron.TaskRunner;
import com.goodgame.profiling.commons.systems.cron.TaskRunner.TaskID;
import com.goodgame.profiling.rewrite_framework.core.drain.AbstractWrappingDrain;
import com.goodgame.profiling.rewrite_framework.core.drain.Drain;

public class RepeatedFlushDrain extends AbstractWrappingDrain {
    private static final Logger log = LogManager.getLogger();

    private final TaskRunner runner;
    private final TaskID task;

    private final Object lock = new Object();

    public RepeatedFlushDrain( Drain inner, TaskRunner runner, Duration frequency ) {
        super( inner );

        this.runner = runner;
        this.task = runner.runRepeated( new Flusher(), "repeating flush", Duration.ZERO, frequency, false );
    }

    @Override
    public void output( List<Metric> metrics ) throws IOException {
        synchronized( lock ) {
            super.output( metrics );
        }
    }

    @Override
    public void flushRemainingBuffers() throws IOException {
        synchronized( lock ) {
            super.flushRemainingBuffers();
        }
    }

    @Override
    public void close() throws IOException {
        synchronized( lock ) {
            runner.stopTask( task );
        }
        super.close();
    }

    private class Flusher implements Runnable {
        @Override
        public void run() {
            try {
                synchronized ( lock ) {
                    RepeatedFlushDrain.this.inner.flushRemainingBuffers();
                }
            } catch( IOException e ) {
                log.warn( "Exception while flushing Buffers", e );
            }
        }
    }
}
