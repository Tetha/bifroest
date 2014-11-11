package com.goodgame.profiling.rewrite_framework.core.drain;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.goodgame.profiling.commons.systems.configuration.EnvironmentWithJSONConfiguration;
import com.goodgame.profiling.rewrite_framework.core.ConfigurationFactory;

public class DrainCreator< E extends EnvironmentWithJSONConfiguration > implements ConfigurationFactory<E, Drain> {

    public static final Logger log = LogManager.getLogger();

    @Override
    public Drain loadConfiguration( E environment, JSONObject config ) {
        JSONObject drainConfig = config.getJSONObject( "drain" );

        log.debug( drainConfig );

        return new DrainFactory<E>().create( environment, drainConfig );
    }
}
