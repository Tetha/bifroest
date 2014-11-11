package com.goodgame.profiling.rewrite_framework.core.source;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.goodgame.profiling.commons.systems.configuration.EnvironmentWithJSONConfiguration;
import com.goodgame.profiling.rewrite_framework.core.ConfigurationFactory;

public class SourceSetCreator< E extends EnvironmentWithJSONConfiguration, U > implements ConfigurationFactory<E, List<SourceSet<U>>> {

    private static final Logger log = LogManager.getLogger();

    private final Class<U> type;

    @SuppressWarnings( "rawtypes" )
    private static final ServiceLoader<SourceSetFactory> factories = ServiceLoader.load( SourceSetFactory.class );

    public SourceSetCreator( Class<U> type ) {
        this.type = type;
    }

    @Override
    public List<SourceSet<U>> loadConfiguration( E environment, JSONObject config ) {
        if ( !config.has( "sources" ) ) {
            return Collections.emptyList();
        }

        List<SourceSet<U>> sources = new LinkedList<>();
        JSONArray sourceArray = config.getJSONArray( "sources" );
        for ( int i = 0; i < sourceArray.length(); i++ ) {
            JSONObject source = sourceArray.getJSONObject( i );
            sources.add( loadSource( environment, source ) );
        }

        return log.exit( Collections.unmodifiableList( sources ) );
    }

    @SuppressWarnings( "unchecked" )
    private SourceSet<U> loadSource( E env, JSONObject config ) {
        for ( SourceSetFactory<E, U> factory : factories ) {

            if ( !factory.handledUnit().isAssignableFrom( type ) ) {
                continue;

            } else if ( !factory.handledType().equalsIgnoreCase( config.getString( "type" ) ) ) {
                continue;

            } else {
                return log.exit( factory.create( env, config ) );
            }
        }
        throw new IllegalArgumentException( "Cannot handle type <" + config.getString( "type" ) + ">" );
    }

}
