package com.goodgame.profiling.rewrite_framework.core.output;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.goodgame.profiling.commons.systems.configuration.EnvironmentWithJSONConfiguration;
import com.goodgame.profiling.rewrite_framework.core.ConfigurationFactory;

public class RewriteRuleCreator< E extends EnvironmentWithJSONConfiguration, I > implements ConfigurationFactory<E, List<RewriteRule<I>>> {

    private static final Logger log = LogManager.getLogger();

    private final Class<I> type;

    @SuppressWarnings( "rawtypes" )
    private static final ServiceLoader<RewriteRuleFactory> factories = ServiceLoader.load( RewriteRuleFactory.class );

    public RewriteRuleCreator( Class<I> type ) {
        this.type = type;
    }

    @Override
    public List<RewriteRule<I>> loadConfiguration( E environment, JSONObject config ) {
        if ( !config.has( "rewrites" ) ) {
            return Collections.emptyList();
        }
        List<RewriteRule<I>> rules = new ArrayList<>();
        JSONObject rewrites = config.getJSONObject( "rewrites" );
        for ( String name : JSONObject.getNames( rewrites ) ) {
            try {
                JSONObject rewrite = rewrites.getJSONObject( name );
                RewriteRule<I> rule = loadRule( environment, rewrite );
                log.debug( "Found " + rule );
                rules.add( rule );
            } catch ( JSONException e ) {
                log.warn( "Ignoring Rewrite rule {}, Reason: {}", name, e.getMessage() );
            }
        }
        return log.exit( Collections.unmodifiableList( rules ) );
    }

    @SuppressWarnings( "unchecked" )
    private RewriteRule<I> loadRule( E env, JSONObject config ) {
        for ( RewriteRuleFactory<E, I> factory : factories ) {

            if ( !factory.handledInput().isAssignableFrom( type ) ) {
                continue;

            } else {
                return log.exit( factory.create( env, config ) );
            }
        }
        throw new IllegalArgumentException( "Cannot handle input type" + type.getName() );
    }

}
