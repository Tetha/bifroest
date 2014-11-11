package com.goodgame.profiling.commons.statistics.eventbus;

import java.time.Clock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.goodgame.profiling.commons.statistics.WriteToStorageEvent;
import com.goodgame.profiling.commons.statistics.storage.MetricStorage;
import com.goodgame.profiling.commons.util.stopwatch.StopWatchWithStates;

public class EventBusImpl implements EventBus {
    private static final Logger log = LogManager.getLogger();
    private final Map<Class<?>, List<EventBusSubscriber<Object>>> subscriberLists;
    private final ExecutorService eventExecutors = Executors.newFixedThreadPool( 1,
            new BasicThreadFactory.Builder().namingPattern( "EventBusThread-%d" ).build() );

    // This duplicates the command with states tracker, I'm aware of this.
    // However, I don't want to use events to track the event processing,
    // because that results in forkbombs and other pleasant things. Hence,
    // I rather do this by hand here.
    //
    // Also I don't use an async clock here, because we usually use the
    // async clock because events could be in the event queue for an 
    // unknown time. If I handle the clock myself, I can assume that 
    // $NOW and event processing time are pretty much the same.
    private final Clock clock = Clock.systemUTC();
    private final StopWatchWithStates stopwatch = new StopWatchWithStates( clock );

    private final LongAdder numberOfEvents = new LongAdder();

    public EventBusImpl() {
        subscriberLists = new HashMap<>();

        subscribe( WriteToStorageEvent.class, e -> {
            stopwatch.consumeStateDurations( (state, duration) -> {
                MetricStorage workStorage = e.storageToWriteTo();
                for ( String s : StringUtils.split( state, '.' ) ) {
                    workStorage = workStorage.getSubStorageCalled( s );
                }
                workStorage.store( "TimeSpent", duration.toNanos() );
            });
            e.storageToWriteTo().getSubStorageCalled( "EventBus" ).store( "EventsFired", numberOfEvents.doubleValue() );
        });
    }
    
    @SuppressWarnings( "unchecked" )
    public < EVENT_CLASS > void subscribe( Class<EVENT_CLASS> event, EventBusSubscriber<EVENT_CLASS> subscriber ) {
        subscriberList( event ).add( (EventBusSubscriber<Object>) subscriber );
    }

    @Override
    public void fire( final Object event ) {
        internalFire( event ); // don't care about finished here
    }

    @Override
    public void synchronousFire( final Object event ) {
        try {
            internalFire( event ).get();
        } catch ( ExecutionException | InterruptedException e ) {
            log.warn( "Interrupted while waiting for processing of event", e );
        }
    }

    @Override
    public void shutdown() throws InterruptedException {
        eventExecutors.shutdown();
        eventExecutors.awaitTermination( 10, TimeUnit.SECONDS );
    }

    private Future<?> internalFire( final Object event ) {
        numberOfEvents.increment();
        return eventExecutors.submit( () -> {
                String metricPrefix = "EventBus.Utilization.Events";
                String eventName = event.getClass().getSimpleName();

                for ( Map.Entry<Class<?>, List<EventBusSubscriber<Object>>> subscriberList : subscriberLists.entrySet() ) {
                    Class<?> eventClass = subscriberList.getKey();
                    List<EventBusSubscriber<Object>> subscribers = subscriberList.getValue();

                    if ( ! eventClass.isAssignableFrom( event.getClass() ) ) continue;

                    for ( EventBusSubscriber<Object> subscriber : subscribers ) {
                        String subscriberName = unlambda( subscriber.getClass().getSimpleName() );
                        String fullStatename = metricPrefix + "." + eventName + "." + subscriberName;
                        stopwatch.startState( fullStatename );

                        try {
                            subscriber.onEvent( event );
                        } catch ( Exception e ) {
                            log.warn( subscriber + " failed", e );
                        }

                        stopwatch.startState( "EventBus.Utilization.idle" );
                    }
                }
                return null;
        } );
    }

    private String unlambda( String simpleNamePotentiallyWithLambda ) {
        return StringUtils.split( simpleNamePotentiallyWithLambda, "$", 2 )[0]; 
    }

    private synchronized List<EventBusSubscriber<Object>> subscriberList( Class<?> c ) {
        if ( subscriberLists.containsKey( c ) ) {
            return subscriberLists.get( c );
        } else {
            List<EventBusSubscriber<Object>> result = new ArrayList<>();
            subscriberLists.put( c, result );
            return result;
        }
    }
}
