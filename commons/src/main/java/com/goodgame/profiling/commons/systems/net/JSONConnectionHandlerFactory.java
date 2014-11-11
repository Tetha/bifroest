package com.goodgame.profiling.commons.systems.net;

import java.net.Socket;
import java.util.Map;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.goodgame.profiling.commons.boot.interfaces.Environment;
import com.goodgame.profiling.commons.systems.net.jsonserver.Command;
import com.goodgame.profiling.commons.systems.net.jsonserver.CommandGroup;

public final class JSONConnectionHandlerFactory<E extends Environment> implements IncomingConnectionHandlerFactory<E> {
    private static final Logger log = LogManager.getLogger();

    private final E environment;
    private final CommandGroup<E> commands;
    private final String interfaceName;

    public JSONConnectionHandlerFactory( E environment, JSONObject config, String interfaceName, Map<String, Command<E>> allCommands ) {
        this.environment = Objects.requireNonNull( environment );
        this.interfaceName = Objects.requireNonNull( interfaceName );
        this.commands = Objects.requireNonNull( findCommands( config, allCommands ) );
    }

    @Override
    public JSONConnectionHandler<E> create( Socket socket ) {
        return new JSONConnectionHandler<E>( environment, socket, interfaceName, commands );
    }

    private CommandGroup<E> findCommands( JSONObject config, Map<String, Command<E>> allCommands ) {
        log.entry( config, allCommands );

        CommandGroup<E> group = new CommandGroup<>( interfaceName );

        if ( config.optString( "commands" ).equalsIgnoreCase( "all" ) ) {
            allCommands.forEach( (commandName, command) -> group.add( command ) );
        } else if ( config.optJSONArray( "commands" ) != null ) {
            for( int i = 0; i < config.getJSONArray( "commands" ).length(); i++ ) {
                String commandName = config.getJSONArray( "commands" ).getString( i );

                group.add( allCommands.get( commandName ) );
            }
        } else {
            throw new IllegalArgumentException( "What do you mean by " + config.get( "commands" ) );
        }

        return log.exit( group );
    }
}
