package com.goodgame.profiling.commons.systems.cron;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.goodgame.profiling.commons.systems.cron.observer.DummyRepeatingTaskObserver;
import com.goodgame.profiling.commons.systems.cron.observer.WatchdogRepeatingTaskObserver;

public class TaskRunner {
    private static final Logger log = LogManager.getLogger();

    private List<StoppableTask> runningTasks = new ArrayList<>();

    @Deprecated
    public synchronized void runOnce( Runnable task, String name, long initialDelay, TimeUnit unit ) {
        OneTimeTask thread = new OneTimeTask( task, name, initialDelay, unit );
        thread.start();
        runningTasks.add( thread );
    }

    public void runOnce( Runnable task, String name, Duration initialDelay ) {
        runOnce( task, name, initialDelay.toNanos(), TimeUnit.NANOSECONDS );
    }

    @Deprecated
    public synchronized TaskID runRepeated( Runnable task, String name, long initialDelay, long frequency, TimeUnit unit, boolean watchdog ) {
        log.info( "Adding repeating task: {}", name );

        RepeatingTask thread;
        if ( watchdog ) {
            thread = new RepeatingTask( task, name, initialDelay, frequency, unit,
                    new WatchdogRepeatingTaskObserver( name + "-repeating-task-watchdog", TimeUnit.MILLISECONDS.convert( frequency, unit ) ) );
        } else {
            thread = new RepeatingTask( task, name, initialDelay, frequency, unit, new DummyRepeatingTaskObserver() );
        }
        thread.start();
        runningTasks.add( thread );
        return new TaskID( thread );
    }

    public TaskID runRepeated( Runnable task, String name, Duration initialDelay, Duration frequency, boolean watchdog ) {
        return runRepeated( task, name, initialDelay.toNanos(), frequency.toNanos(), TimeUnit.NANOSECONDS, watchdog );
    }

    public synchronized void stopTask( TaskID taskId ) {
        StoppableTask task = taskId.task;
        
        log.info( "Telling task {} to shut down", task.toString() );

        task.stopYourself();
        try {
            log.info( "Joining task {}", task.toString() );
            task.join();
        } catch( InterruptedException e ) {
            log.warn( "Interrupted while joining thread", e );
        }

        runningTasks.remove( task );
    }

    public synchronized void shutdown() {
        for( StoppableTask task : runningTasks ) {
            log.info( "Telling task {} to shut down", task.toString() );
            task.stopYourself();
        }
        for( StoppableTask task : runningTasks ) {
            try {
                log.info( "Joining task {}", task.toString() );
                task.join();
            } catch( InterruptedException e ) {
                log.warn( "Interrupted while joining thread", e );
            }
        }
    }

    public class TaskID {
        private final StoppableTask task;

        private TaskID( StoppableTask task ) {
            this.task = task;
        }
    }
}
