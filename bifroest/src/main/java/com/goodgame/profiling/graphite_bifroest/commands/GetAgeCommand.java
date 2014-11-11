package com.goodgame.profiling.graphite_bifroest.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.kohsuke.MetaInfServices;

import com.goodgame.profiling.commons.systems.configuration.EnvironmentWithJSONConfiguration;
import com.goodgame.profiling.commons.systems.net.jsonserver.Command;
import com.goodgame.profiling.graphite_bifroest.systems.prefixtree.EnvironmentWithPrefixTree;

@MetaInfServices
public class GetAgeCommand< E extends EnvironmentWithJSONConfiguration & EnvironmentWithPrefixTree > implements Command<E> {
    private static final Logger log = LogManager.getLogger();

    @Override
    public String getJSONCommand() {
        return "get-metric-age";
    }

    @Override
    public List<Class<? super E>> getRequiredEnvironments() {
        return Collections.<Class<? super E>> singletonList( EnvironmentWithPrefixTree.class );
    }

    @Override
    public List<Pair<String, Boolean>> getParameters() {
        return Collections.<Pair<String, Boolean>> singletonList( new ImmutablePair<>( "metric-prefix", true ) );
    }

    @Override
    public JSONObject execute( JSONObject input, E environment ) {
        log.entry( input, environment );

        List<String> blacklist = new ArrayList<>();
        environment.getConfigurationLoader().loadConfiguration();
        JSONObject config = environment.getConfiguration().getJSONObject( "bifroest" );
        JSONArray blackarray = config.getJSONArray( "blacklist" );
        for ( int i = 0; i < blackarray.length(); i++ ) {
            blacklist.add( blackarray.getString( i ) );
        }

        String prefix = input.getString( "metric-prefix" );
        long age = environment.getTree().findAge( prefix, blacklist );
        log.trace( "Prefix {}, Blacklist {}, age {}", prefix, blacklist, age );

        // Future timestamp is bad, and PrefixTree defaults to Long.MAX_VALUE
        if ( age > System.currentTimeMillis() / 1000 ) {
            return log.exit( new JSONObject().put( "found", false ) );
        } else {
            return log.exit( new JSONObject().put( "found", true ).put( "age", age ) );
        }
    }
}
