package com.goodgame.profiling.stream_rewriter;

import java.io.IOException;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.goodgame.profiling.commons.systems.configuration.EnvironmentWithJSONConfiguration;
import com.goodgame.profiling.commons.systems.net.IncomingConnectionHandlerFactory;
import com.goodgame.profiling.rewrite_framework.core.drain.Drain;
import com.goodgame.profiling.rewrite_framework.core.drain.DrainCreator;
import com.goodgame.profiling.stream_rewriter.source.handler.StreamLineHandler;

public final class MetricConnectionHandlerFactory<E extends EnvironmentWithJSONConfiguration> implements IncomingConnectionHandlerFactory<E> {
    private static final Logger log = LogManager.getLogger();

    private final Drain drain;
    private final String name;

    public MetricConnectionHandlerFactory( E environment, String name, JSONObject config ) {
        this.drain = new DrainCreator<>().loadConfiguration( environment, config );
        this.name = name;
    }

    @Override
    public MetricConnectionHandler create( Socket socket ) {
        return new MetricConnectionHandler( socket, name, new StreamLineHandler( drain ) );
    }

    @Override
    public void shutdown() {
        try {
            drain.flushRemainingBuffers();
        } catch( IOException e ) {
            log.warn( "Exception while flushing buffers", e );
        }
        try {
            drain.close();
        } catch( IOException e ) {
            log.warn( "Exception while closing drains", e );
        }
    }
}
