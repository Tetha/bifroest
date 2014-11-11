package com.goodgame.profiling.commons.statistics.eventbus.disruptor;

import java.util.concurrent.CountDownLatch;

public class StatisticEventHolder {
    private Object event;
    private CountDownLatch latch;

    public void set( Object newEvent ) {
        this.event = newEvent;
    }
    
    public Object get() {
        return this.event;
    }

    public void setLatch( CountDownLatch latch ) {
        this.latch = latch;
    }

    public void countDownLatchIfExists() {
        if ( this.latch != null ) {
            this.latch.countDown();
        }
    }
}
