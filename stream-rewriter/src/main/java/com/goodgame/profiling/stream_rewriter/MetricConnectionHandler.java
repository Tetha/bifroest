package com.goodgame.profiling.stream_rewriter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.goodgame.profiling.commons.statistics.ProgramStateChanged;
import com.goodgame.profiling.commons.statistics.eventbus.EventBusManager;
import com.goodgame.profiling.commons.systems.net.IncomingConnectionHandler;
import com.goodgame.profiling.commons.systems.net.ServerThread;
import com.goodgame.profiling.stream_rewriter.source.handler.StreamLineHandler;

public final class MetricConnectionHandler implements IncomingConnectionHandler {
    private static final Logger log = LogManager.getLogger();

    private final Instant createdAt = Clock.systemUTC().instant();
    private final Socket socket;
    private final String name;
    private final StreamLineHandler lineHandler;

    public MetricConnectionHandler( Socket socket, String name, StreamLineHandler lineHandler ) {
        this.socket = socket;
        this.name = name;
        this.lineHandler = lineHandler;
    }

    @Override
    public void run() {
        // deliberately create events in the past to get thread IDs right
        EventBusManager.fire( new ProgramStateChanged( ServerThread.CLIENT_TIMING + name, Optional.of( "queued" ), Thread.currentThread().getId(), createdAt ) );
        ProgramStateChanged.fireContextChangeToState( ServerThread.CLIENT_TIMING + name, "execution" );
        String oldName = Thread.currentThread().getName();
        Thread.currentThread().setName( oldName + " [" + socket.getRemoteSocketAddress() + "]" );
        try( BufferedReader reader = new BufferedReader( new InputStreamReader( socket.getInputStream() ) )) {
            String line;
            while( ( line = reader.readLine() ) != null ) {
                lineHandler.handleUnit( line );
            }
        } catch( Exception e ) {
            log.warn( "Unexpected Exception while serving " + socket.getRemoteSocketAddress().toString(), e );
        } finally {
            try {
                ProgramStateChanged.fireContextStopped( ServerThread.CLIENT_TIMING + name );
                Thread.currentThread().setName( oldName );
                socket.close();
            } catch( IOException e ) {
                log.warn( "Exception while closing socket:", e );
            }
        }
    }
}
