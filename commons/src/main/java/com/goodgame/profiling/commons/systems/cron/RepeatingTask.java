package com.goodgame.profiling.commons.systems.cron;

import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.goodgame.profiling.commons.systems.cron.observer.RepeatingTaskObserver;

class RepeatingTask extends Thread implements StoppableTask {

    private static final Logger log = LogManager.getLogger();

    private final Runnable task;
    private final long initialDelayInMillis;
    private final long frequencyInMillis;
    private final RepeatingTaskObserver observer;

    private volatile boolean stop = false;
    private volatile boolean sleeping = false;

    public RepeatingTask( Runnable task, String name, long initialDelay, long frequency, TimeUnit unit, RepeatingTaskObserver observer ) {
        super( name );
        this.task = task;
        this.initialDelayInMillis = TimeUnit.MILLISECONDS.convert( initialDelay, unit );
        this.frequencyInMillis = TimeUnit.MILLISECONDS.convert( frequency, unit );
        this.observer = observer;
    }

    @Override
    public void run() {
        try {
            Thread.sleep( initialDelayInMillis );
        } catch ( InterruptedException e ) {
            // ignore
            // During shutdown, stop will be true, and the following loop will
            // be skipped
        }
        while ( !stop ) {
            long start = System.currentTimeMillis();
            observer.threadStarted( start );
            try {
                task.run();
            } catch ( Exception e ) {
                log.warn( "Task execution failed", e );
            }
            observer.threadStopped( start );

            if ( !stop ) {
                try {
                    sleeping = true;
                    Thread.sleep( frequencyInMillis - ( System.currentTimeMillis() - start ) );
                } catch ( InterruptedException | IllegalArgumentException e ) {
                    // ignore
                } finally {
                    sleeping = false;
                }
            }
        }
    }

    @Override
    public void stopYourself() {
        stop = true;
        if ( sleeping ) {
            this.interrupt();
        }
    }
    
    @Override
    public String toString() {
        return "RepeatingTask " + getName();
    }
}
