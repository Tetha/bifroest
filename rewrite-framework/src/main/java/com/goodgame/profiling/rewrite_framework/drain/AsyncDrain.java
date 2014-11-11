package com.goodgame.profiling.rewrite_framework.drain;

import java.io.IOException;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.goodgame.profiling.commons.model.Metric;
import com.goodgame.profiling.commons.statistics.eventbus.EventBusManager;
import com.goodgame.profiling.rewrite_framework.core.drain.Drain;
import com.goodgame.profiling.rewrite_framework.drain.statistics.AsyncDrainQueueSizeChangedEvent;

public class AsyncDrain implements Drain {
    private static final Logger log = LogManager.getLogger();

    private static final Clock clock = Clock.systemUTC();

    private final Drain innerDrain;

    private final BlockingQueue<Metric> outputQueue;
    private final int maxQueueSize;

    // Use a executor service, so dying threads are restarted. This needs to be single-threaded.
    private final ExecutorService queueConsumerExecutor;

    private volatile boolean stopped;

    public AsyncDrain( Drain innerDrain, int maxQueueSize ) {
        this.innerDrain = innerDrain;

        this.maxQueueSize = maxQueueSize;
        this.outputQueue = new LinkedBlockingQueue<Metric>( maxQueueSize );

        this.queueConsumerExecutor = Executors.newSingleThreadExecutor( r -> {
            Thread t = new Thread( r );
            t.setName( "Queue Consumer Thread" );
            return t;
        });
        this.queueConsumerExecutor.submit( new QueueConsumer() );
    }

    @Override
    public void flushRemainingBuffers() throws IOException {
        log.debug( "Stopping queue consumer" );
        stopped = true;
        log.debug( "Shutting down executor service" );
        queueConsumerExecutor.shutdownNow();
        log.debug( "Awaiting termination of executor service" );
        try {
            queueConsumerExecutor.awaitTermination( Long.MAX_VALUE, TimeUnit.DAYS );
        } catch( InterruptedException e ) {
            log.warn( "Interrupted while awaiting termination of queue consumer", e );
        }
        log.debug( "Sending remaining buffers" );
        sendRemainingBuffer();
        log.debug( "Flushing inner drain" );
        innerDrain.flushRemainingBuffers();
        log.debug( "All metrics flushed" );
    }

    @Override
    public void close() throws IOException {
        // nothing
    }

    @Override
    public void output( List<Metric> metrics ) throws IOException {
        for( Metric metric : metrics ) {
            try {
                outputQueue.put( metric );
                fireAsyncDrainQueueSizeEvent( 1 );
            } catch( InterruptedException e ) {
                log.warn( "Interrupted while trying to add metrics to queue", e );
            }
        }
    }

    private void fireAsyncDrainQueueSizeEvent( int delta ) {
        EventBusManager.fire( new AsyncDrainQueueSizeChangedEvent( clock.instant(), delta ) );
    }
    
    private void sendRemainingBuffer() throws IOException {
        log.debug( "sending " + outputQueue.size() + " elements " );
        List<Metric> metrics = new ArrayList<>( maxQueueSize );
        do {
            metrics.clear();
            outputQueue.drainTo( metrics, maxQueueSize );
            fireAsyncDrainQueueSizeEvent( -metrics.size() );
            innerDrain.output( metrics );
        } while( metrics.size() != 0 );
    }

    private class QueueConsumer implements Runnable {
        public QueueConsumer() {
            log.trace( "Created queue consumer" );
        }

        @Override
        public void run() {
            log.trace( "QueueConsumer up and running" );

            try {
                if( !stopped ) {
                    // Simulate a loop by rescheduling ourselves.
                    queueConsumerExecutor.submit( new QueueConsumer() );

                    List<Metric> metrics = new ArrayList<>( maxQueueSize );

                    // Take blocks, so we don't run in a hot-loop
                    Metric metric = outputQueue.take();
                    metrics.add( metric );

                    // Be performant: drain as much as possible. Unfortunately,
                    // there is no blocking version of this, so we need the take() above
                    outputQueue.drainTo( metrics, maxQueueSize );

                    fireAsyncDrainQueueSizeEvent( -metrics.size() );

                    innerDrain.output( metrics );
                }
            } catch( IOException | RuntimeException e ) {
                log.warn( "Sending data failed ", e );
            } catch( InterruptedException e ) {
                log.debug( " OutputThread interrupted " );
            }
            log.trace( " OutputThread exited - a new one should spawn right now " );
        }
    }
}
