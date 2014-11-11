package com.goodgame.profiling.rewrite_framework.core.output.generator;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.goodgame.profiling.commons.systems.configuration.EnvironmentWithJSONConfiguration;
import com.goodgame.profiling.rewrite_framework.core.ConfigurationFactory;

public class MetricNameGeneratorCreator< E extends EnvironmentWithJSONConfiguration > implements ConfigurationFactory<E, List<MetricNameGenerator>> {

    private static final Logger log = LogManager.getLogger();

    @Override
    public List<MetricNameGenerator> loadConfiguration( E env, JSONObject config ) {
        List<MetricNameGenerator> generators = new LinkedList<MetricNameGenerator>();

        // use non-throwing version here so we can deal with old object
        JSONObject outputSpec = config.optJSONObject( "output" );

        MetricNameGeneratorFactory<E> generatorFactory = new MetricNameGeneratorFactory<E>();
        if ( outputSpec != null ) {
            MetricNameGenerator generator = generatorFactory.create( env, outputSpec );
            generators.add( generator );

        } else {
            // use throwing version here so we get informed if it's neither an
            // array nor an object
            JSONArray outputSpecs = config.getJSONArray( "output" );
            for ( int i = 0; i < outputSpecs.length(); i++ ) {
                JSONObject singleOutputSpec = outputSpecs.getJSONObject( i );
                generators.add( generatorFactory.create( env, singleOutputSpec ) );
            }
        }
        return log.exit( Collections.unmodifiableList( generators ) );
    }

}
