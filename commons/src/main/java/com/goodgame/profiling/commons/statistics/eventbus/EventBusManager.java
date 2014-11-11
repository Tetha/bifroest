package com.goodgame.profiling.commons.statistics.eventbus;

import java.util.Objects;

public class EventBusManager {
    //private static EventBus eventBus = new EventBusImpl();
    private static EventBus eventBus = null;
    
    public static EventBus getEventBus() {
        return eventBus;
    }
    
    public static void setEventBus(EventBus newEventBus) {
        eventBus = Objects.requireNonNull( newEventBus );
    }
    
    private static void ensureEventBusExists() {
        if ( eventBus == null  ) {
            eventBus = new EventBusImpl();
        }
    }

    public static void fire( final Object event ) {
        ensureEventBusExists();
        eventBus.fire( event );
    }
    public static void synchronousFire( final Object event ) {
        ensureEventBusExists();
        eventBus.synchronousFire( event );
    }

    public static < EVENT_CLASS > void subscribe( Class<EVENT_CLASS> event, EventBusSubscriber<EVENT_CLASS> subscriber ) {
        ensureEventBusExists();
        eventBus.subscribe( event, subscriber );
    }
}
