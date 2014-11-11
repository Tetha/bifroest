package com.goodgame.profiling.rewrite_framework.systems.gatherer;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import com.goodgame.profiling.commons.statistics.eventbus.EventBusManager;
import com.goodgame.profiling.commons.statistics.units.parse.DurationParser;
import com.goodgame.profiling.commons.systems.configuration.EnvironmentWithJSONConfiguration;
import com.goodgame.profiling.commons.systems.cron.EnvironmentWithTaskRunner;
import com.goodgame.profiling.commons.systems.cron.TaskRunner.TaskID;
import com.goodgame.profiling.commons.systems.statistics.EnvironmentWithStatisticsGatherer;
import com.goodgame.profiling.rewrite_framework.core.config.FetchConfiguration;
import com.goodgame.profiling.rewrite_framework.core.config.FetchConfigurationFromConfiguration;
import com.goodgame.profiling.rewrite_framework.core.source.Source;
import com.goodgame.profiling.rewrite_framework.core.source.SourceSet;
import com.goodgame.profiling.rewrite_framework.core.source.handler.SourceHandlerCreator;
import com.goodgame.profiling.rewrite_framework.core.source.handler.SourceUnitHandler;
import com.goodgame.profiling.rewrite_framework.core.source.timestamp.Timestamp;
import com.goodgame.profiling.rewrite_framework.statistics.SourceFetchFinishedEvent;
import com.goodgame.profiling.rewrite_framework.statistics.SourceFetchStartedEvent;
import com.goodgame.profiling.rewrite_framework.statistics.UpdateFinishedEvent;
import com.goodgame.profiling.rewrite_framework.statistics.UpdateStartedEvent;
import com.goodgame.profiling.rewrite_framework.systems.gatherer.monitoring.FetchConfigurationLoadedEvent;
import com.goodgame.profiling.rewrite_framework.systems.gatherer.monitoring.SourceWatchdog;
import com.goodgame.profiling.rewrite_framework.systems.gatherer.optimization.ThreadCountOptimizer;

public final class Fetcher< E extends EnvironmentWithJSONConfiguration & EnvironmentWithStatisticsGatherer & EnvironmentWithTaskRunner, I, U > implements Runnable {

    private static final Logger log = LogManager.getLogger();

    private final E environment;
    private final SourceHandlerCreator<I, U> handlerCreator;
    private final Class<I> inputType;
    private final Class<U> unitType;

    private final ThreadPoolExecutor fetchPool;
    private final ThreadCountOptimizer optimizer;

    private final SourceWatchdog sourceWatchdog = new SourceWatchdog();
    private final Duration sourceWatchdogInterval;

    private long startTimeMillis;

    public Fetcher( E environment, Class<I> inputType, Class<U> unitType ) {
        this.environment = environment;
        handlerCreator = new SourceHandlerCreator<>( inputType, unitType );
        this.inputType = inputType;
        this.unitType = unitType;

        this.optimizer = ThreadCountOptimizer.withDefaultStrategies( environment );
        this.sourceWatchdogInterval = ( new DurationParser() ).parse(
                environment.getConfiguration().getJSONObject( "fetcher" ).getString( "source-watchdog-interval" ) );

        ThreadFactory threads = new BasicThreadFactory.Builder().namingPattern( "FetchThread[initial]" ).build();
        fetchPool = new ThreadPoolExecutor(
                1, 1, // thread count is set to the real initial value on the first run()
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(),
                threads
        );
    }

    private void setPoolSize( int poolSize ) {
        fetchPool.setCorePoolSize( poolSize );
        fetchPool.setMaximumPoolSize( poolSize );
    }

    @Override
    public synchronized void run() {
        try {
            startTimeMillis = System.currentTimeMillis();

            setPoolSize( optimizer.nextTreadCount() );

            environment.getConfigurationLoader().loadConfiguration();

            FetchConfiguration<I, U> fetchConf = new FetchConfigurationFromConfiguration<>( inputType, unitType, environment, environment.getConfiguration() );

            EventBusManager.fire( new FetchConfigurationLoadedEvent( fetchConf ) );

            executeFetchConfiguration( startTimeMillis / 1000, fetchConf );
        } catch (Exception e) {
            log.warn( "Exception in Fetcher.run - broken fetch Config?", e );
        } finally {
            optimizer.recordRuntime( System.currentTimeMillis() - startTimeMillis );
        }
    }

    /**
     * Test-Only method to execute fetches.
     */
    public void executeFetchConfiguration( long startTimestamp, FetchConfiguration<I, U> fetchConfig ) {
        boolean successful = true;
        EventBusManager.fire( new UpdateStartedEvent( Clock.systemUTC() ) );
        log.debug( "starting fetch since " + startTimestamp );

        try {
            Map<String, Future<Object>> fetches = new HashMap<>();
            Map<String, Duration> abortAfterForSourceId = new HashMap<>();

            for ( SourceSet<U> sourceSet : fetchConfig.sources() ) {
                List<Source<U>> sources = sourceSet.generateSources();

                for ( Source<U> source : sources ) {
                    SingleFetch fetch = new SingleFetch( startTimestamp, source, sourceSet.timestamp(), fetchConfig );
                    fetches.put( source.sourceId(), fetchPool.submit( Executors.callable( fetch ) ) );
                    log.debug( "Submitted fetch for source: {} - {}", source.sourceId(), fetch );
                    abortAfterForSourceId.put( source.sourceId(), sourceSet.abortFetchAfter() );
                }
            }

            sourceWatchdog.setAbortAfterForSourceId( abortAfterForSourceId );
            TaskID watchdog = environment.taskRunner().runRepeated( new SourceWatchdog.SourceWatchdogRunnable( fetches ), "SourceWatchdog", Duration.ZERO, sourceWatchdogInterval, false );
            try {
                for ( Map.Entry<String, Future<Object>> f : fetches.entrySet() ) {
                    try {
                        log.debug( "Waiting for Future for " + f.getKey() );
                        f.getValue().get();
                    } catch ( CancellationException e ) {
                        log.warn( "Future for " + f.getKey() + " cancelled", e );
                    } catch ( ExecutionException e ) {
                        log.warn( "Fetch for " + f.getKey() + " failed", e );
                    }
                }
            } catch ( InterruptedException e ) {
                log.warn( "Update interrupted", e );
                successful = false;
            }
            environment.taskRunner().stopTask( watchdog );

            try {
                fetchConfig.drain().flushRemainingBuffers();
            } catch( IOException e ) {
                log.warn( "Exception while flushing buffers", e );
                successful = false;
            }
            try {
                fetchConfig.drain().close();
            } catch( IOException e ) {
                log.warn( "Exception while closing drains", e );
                successful = false;
            }
        } finally {
            EventBusManager.synchronousFire( new UpdateFinishedEvent( Clock.systemUTC(), successful ) );
        }
    }
    // synchronize with run, so that run can schedule all its fetches
    public synchronized void shutdown() {
        fetchPool.shutdown();
        try {
            fetchPool.awaitTermination( Long.MAX_VALUE, TimeUnit.DAYS );
        } catch ( InterruptedException e ) {
            log.warn( "Interrupted on shutdown", e );
        }
    }

    private final class SingleFetch implements Runnable {

        private final Source<U> source;
        private final SourceUnitHandler<U> handler;
        private final Timestamp timestamp;
        private final int numRules;
        private final long now;

        public SingleFetch( long start, Source<U> s, Timestamp t, FetchConfiguration<I, U> fetchConf ) {
            source = s;
            timestamp = t;
            handler = handlerCreator.create( source, fetchConf );
            numRules = fetchConf.rules().size();
            now = start;
        }

        @Override
        public void run() {
            try {
                boolean successful = false;

                Thread.currentThread().setName( "FetchThread[" + source.sourceId() + "]" );
                ThreadContext.put("sourceId", source.sourceId());

                EventBusManager.fire( new SourceFetchStartedEvent( source.sourceId(), numRules, System.currentTimeMillis() ) );
                successful = source.load( timestamp, now, handler );
                EventBusManager.fire( new SourceFetchFinishedEvent( source.sourceId(), System.currentTimeMillis(), successful ) );

                Thread.currentThread().setName( "FetchThread[none]" );
                ThreadContext.put("sourceId", "[none]");
            } catch ( Exception e ) {
                log.warn( "A totally unexpected exception occured during single fetch: ", e );
            }
        }

        @Override
        public String toString() {
            return "SingleFetch [source=" + source + ", handler=" + handler + ", timestamp=" + timestamp + ", numRules=" + numRules + ", now=" + now + "]";
        }
    }
}
