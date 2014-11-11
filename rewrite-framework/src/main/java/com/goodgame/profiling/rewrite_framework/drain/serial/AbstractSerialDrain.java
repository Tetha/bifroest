package com.goodgame.profiling.rewrite_framework.drain.serial;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.goodgame.profiling.rewrite_framework.core.drain.Drain;

public abstract class AbstractSerialDrain implements Drain {
    private final Logger log = LogManager.getLogger();

    private final Collection<Drain> subs;

    protected AbstractSerialDrain( Collection<Drain> subs ) {
        this.subs = subs;
    }

    protected interface ConsumerWithIOException<T> {
        void accept( T t ) throws IOException;
    }

    protected Collection<Drain> drains() {
        return Collections.unmodifiableCollection( this.subs );
    }

    protected void forEachDrainLoggingExceptions( ConsumerWithIOException<Drain> c ) {
        subs.forEach( s -> {
            try {
                c.accept( s );
            } catch ( Exception e ) {
                log.warn( "Drain " + s + " failed to do ", e );
            }
        });
    }

    @Override
    public void flushRemainingBuffers() {
        forEachDrainLoggingExceptions( Drain::flushRemainingBuffers );
    }

    @Override
    public void close() {
        forEachDrainLoggingExceptions( Drain::close );
    }
}
