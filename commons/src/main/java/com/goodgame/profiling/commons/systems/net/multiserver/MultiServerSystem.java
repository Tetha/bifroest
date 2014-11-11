package com.goodgame.profiling.commons.systems.net.multiserver;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.goodgame.profiling.commons.boot.interfaces.Subsystem;
import com.goodgame.profiling.commons.systems.SystemIdentifiers;
import com.goodgame.profiling.commons.systems.configuration.EnvironmentWithJSONConfiguration;
import com.goodgame.profiling.commons.systems.cron.EnvironmentWithTaskRunner;
import com.goodgame.profiling.commons.systems.net.IncomingConnectionHandlerFactoryFactory;
import com.goodgame.profiling.commons.systems.net.ServerThread;
import com.goodgame.profiling.commons.systems.net.throttle.LinearThrottleControl;
import com.goodgame.profiling.commons.systems.net.throttle.TimeBasedSensor;

public class MultiServerSystem< E extends EnvironmentWithJSONConfiguration & EnvironmentWithTaskRunner > implements Subsystem<E> {
    private static final Logger log = LogManager.getLogger();

    private static final Clock clock = Clock.systemUTC();

    private final List<ServerThread<E>> serverThreads = new ArrayList<>();

    @Override
    public String getSystemIdentifier() {
        return SystemIdentifiers.MULTI_SERVER;
    }

    @Override
    public Collection<String> getRequiredSystems() {
        return Arrays.asList( SystemIdentifiers.LOGGING, SystemIdentifiers.CONFIGURATION, SystemIdentifiers.STATISTICS, SystemIdentifiers.CRON );
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public void boot( E environment ) {
        JSONObject config = environment.getConfiguration().getJSONObject( "multi-server" );
        JSONArray interfaces = config.getJSONArray( "interfaces" );

        Map<String, IncomingConnectionHandlerFactoryFactory<E>> factories = new HashMap<>();
        for (IncomingConnectionHandlerFactoryFactory<E> factory
                : ServiceLoader.load(IncomingConnectionHandlerFactoryFactory.class)) {
            factories.put(factory.handledFormat(), factory);
        }
        for (int i = 0; i < interfaces.length(); i++) {
            try {
                JSONObject interfaceConfig = interfaces.getJSONObject( i );
                if( !interfaceConfig.getString( "type" ).equals( "tcp" ) ) {
                    throw new IllegalArgumentException( "Cannot handle " + interfaceConfig.getString( "type" ) );
                }
                String format = interfaceConfig.getString( "format" );
                IncomingConnectionHandlerFactoryFactory<E> handlerFactory = Objects.requireNonNull( factories.get( format ) );
                ServerThread<E> serverThread = new ServerThread<E>(
                        environment,
                        interfaceConfig,
                        new LinearThrottleControl( new TimeBasedSensor( clock, clock.instant(), Duration.ofMinutes( 1 ) ) ),
                        handlerFactory.createFactory( environment, interfaceConfig.getString( "name" ) )
                );
                serverThread.start();
                log.info( "Started serverthread {}", interfaceConfig.getString( "name" ) );
                serverThreads.add( serverThread );
            } catch ( IOException e ) {
                throw new RuntimeException( e );
            }
        }
    }

    @Override
    public void shutdown( E environment ) {
        serverThreads.forEach( serverThread -> serverThread.shutdown() );
        serverThreads.forEach( serverThread -> {
            log.info( "Joining {}", serverThread.getName() );
            try {
                serverThread.join();
            } catch ( InterruptedException e ) {
                log.warn( "Interrupted while joining JSON server thread", e );
            }
        } );
    }

    public static < E extends EnvironmentWithJSONConfiguration > Map<String, JSONObject> createInterfaceConfigs( E environment ) {
        JSONArray interfaces = environment.getConfiguration().getJSONObject( "multi-server" ).getJSONArray( "interfaces" );

        Map<String, JSONObject> ret = new HashMap<>();
        for ( int i = 0; i < interfaces.length(); i++ ) {
            ret.put( interfaces.getJSONObject( i ).getString( "name" ), interfaces.getJSONObject( i ) );
        }
        return ret;
    }
}
