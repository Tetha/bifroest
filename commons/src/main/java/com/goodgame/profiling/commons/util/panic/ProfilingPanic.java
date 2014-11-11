package com.goodgame.profiling.commons.util.panic;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.goodgame.profiling.commons.statistics.jmx.MBeanManager;

public enum ProfilingPanic implements ProfilingPanicMBean {
    INSTANCE;

    private static final Logger log = LogManager.getLogger();

    private final List<PanicAction> actions;
    private final Map<PanicAction, Instant> nextPossiblePanic;

    private ProfilingPanic( ) {
        this.actions = new ArrayList<>();
        this.nextPossiblePanic = new HashMap<>();

        MBeanManager.registerStandardMBean( this, ProfilingPanic.class.getPackage().getName() + ":type=" + ProfilingPanic.class.getSimpleName(),
                ProfilingPanicMBean.class );
    }

    public synchronized void addAction( PanicAction action ) {
        Instant now = Instant.now();

        actions.add( action );
        nextPossiblePanic.put( action, now );
    }

    @Override
    public synchronized void panic() {
        Instant now = Instant.now();

        for( PanicAction action : actions ) {
            potentiallyExecuteAction( action, now );
        }
    }

    @Override
    public synchronized void dontPanic( Duration relaxTime ) {
        Instant now = Instant.now();

        for( PanicAction action : actions ) {
            if ( nextPossiblePanic.get( action ).isBefore( now.plus( relaxTime ) ) ) {
                nextPossiblePanic.put( action, now.plus( relaxTime ) );
            }
        }
    }

    private void potentiallyExecuteAction( PanicAction action, Instant now ) {
        if ( nextPossiblePanic.get( action ).isBefore( now ) ) {
            log.info( "Executing " + action.getClass().getName() );
            action.execute( now );
            nextPossiblePanic.put( action, now.plus( action.getCooldown() ) );
        }
    }
}
