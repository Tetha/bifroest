package com.goodgame.profiling.commons.systems.net;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.kohsuke.MetaInfServices;

import com.goodgame.profiling.commons.boot.interfaces.Environment;
import com.goodgame.profiling.commons.systems.configuration.EnvironmentWithJSONConfiguration;
import com.goodgame.profiling.commons.systems.net.jsonserver.Command;
import com.goodgame.profiling.commons.systems.net.multiserver.MultiServerSystem;

@MetaInfServices
public final class JSONConnectionHandlerFactoryFactory<E extends EnvironmentWithJSONConfiguration> implements IncomingConnectionHandlerFactoryFactory<E> {
	private static final Logger log = LogManager.getLogger();
	
    private Map<String, JSONObject> interfaceConfigs; // quasi-final
    private Map<String, Command<E>> allCommands; // quasi-final

    @Override
    public String handledFormat() {
        return "json";
    }

    @Override
    public JSONConnectionHandlerFactory<E> createFactory( E environment, String interfaceName ) {
        synchronized ( this ) {
            if ( allCommands == null ) {
                this.allCommands = createAllCommands( environment );
                this.interfaceConfigs = MultiServerSystem.createInterfaceConfigs( environment );
            }
        }
        return new JSONConnectionHandlerFactory<E>( environment, interfaceConfigs.get( interfaceName ), interfaceName, allCommands );
    }

    @SuppressWarnings( "unchecked" )
    private static < E extends Environment > Map<String, Command<E>> createAllCommands( E environment ) {
        Map<String, Command<E>> commands = new HashMap<>();
        for ( Command<E> command : ServiceLoader.load( Command.class ) ) {
        	log.trace( "Considering command " + command);
            boolean doAdd = true;
            for ( Class<? super E> env : command.getRequiredEnvironments() ) {
                doAdd &= env.isAssignableFrom( environment.getClass() );
            }
            if ( doAdd ) {
            	log.info( "Adding " + command );
                commands.put( command.getJSONCommand(), command );
            } else {
            	log.trace( "Not adding " + command + " due to environment types" );
            }
        }
        return commands;
    }
}
