package com.goodgame.profiling.rewrite_framework.systems.persistent_drains;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.goodgame.profiling.commons.systems.configuration.EnvironmentWithJSONConfiguration;
import com.goodgame.profiling.commons.util.json.JSONUtils;
import com.goodgame.profiling.rewrite_framework.drain.persistent.PersistentDrain;
import com.goodgame.profiling.rewrite_framework.drain.persistent.PersistentDrainFactory;

public class PersistentDrainManager<E extends EnvironmentWithJSONConfiguration> {
    private static final Logger log = LogManager.getLogger();

    private Map<String, PersistentDrain> drains = new HashMap<>();

    @SuppressWarnings( "unchecked" )
    public PersistentDrainManager( E env ) {
        JSONObject config = env.getConfiguration().getJSONObject( "persistent drains" );

        List<PersistentDrainFactory<E, ? extends PersistentDrain>> factories = new ArrayList<>();
        for( PersistentDrainFactory<E, ? extends PersistentDrain> factory : ServiceLoader.load( PersistentDrainFactory.class ) ) {
            factories.add( factory );
            log.debug( "PersistentDrainFactory loaded for " + factory.handledType() );
        }

        drainConfigLoop:
        for( String id : JSONUtils.keys( config ) ) {
            JSONObject persistentDrainConfig = config.getJSONObject( id );

            for( PersistentDrainFactory<E, ? extends PersistentDrain> factory : factories ) {
                if ( factory.handledType().equals( persistentDrainConfig.getString( "type" ) ) ) {
                    drains.put( id, factory.create( env, persistentDrainConfig ) );
                    continue drainConfigLoop;
                }
            }
            log.warn( "No PersistentDrainFactory found for type " + persistentDrainConfig.getString( "type" ) + " while configuring id " + id );
        }
    }

    public PersistentDrain getPersistentDrain( String id ) {
        return drains.get( id );
    }

    public boolean hasPersistentDrain( String id ) {
        return drains.containsKey( id );
    }

    public Map<String, PersistentDrain> getAllPersistentDrains() {
        return drains;
    }

    public void shutdown() {
        for( PersistentDrain drain : drains.values() ) {
            drain.shutdown();
        }
    }
}
