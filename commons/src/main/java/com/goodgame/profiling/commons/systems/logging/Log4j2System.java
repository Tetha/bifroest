package com.goodgame.profiling.commons.systems.logging;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Collection;
import java.util.Collections;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.goodgame.profiling.commons.boot.interfaces.Environment;
import com.goodgame.profiling.commons.boot.interfaces.Subsystem;
import com.goodgame.profiling.commons.systems.SystemIdentifiers;
import com.goodgame.profiling.commons.util.panic.PanicAnnouncement;
import com.goodgame.profiling.commons.util.panic.ProfilingPanic;

public class Log4j2System<E extends Environment> implements Subsystem<E> {
    private static final Logger log = LogManager.getLogger();

    @Override
    public String getSystemIdentifier() {
        return SystemIdentifiers.LOGGING;
    }

    @Override
    public Collection<String> getRequiredSystems() {
        return Collections.emptyList();
    }

    @Override
    public void boot( E environment ) {
        LogManager.getLogger().info( "Logging initialized" );

        Thread.setDefaultUncaughtExceptionHandler( new UncaughtExceptionHandler() {
            @Override
            public void uncaughtException( Thread t, Throwable e ) {
                log.error( "Exception in thread " + t.getName(), e );
            }
        } );

        ProfilingPanic.INSTANCE.addAction( new PanicAnnouncement() );
    }

    @Override
    public void shutdown( E environment ) {
        // do nothing
    }

}
