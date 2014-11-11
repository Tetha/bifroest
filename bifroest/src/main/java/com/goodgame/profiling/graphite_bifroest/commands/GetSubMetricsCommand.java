package com.goodgame.profiling.graphite_bifroest.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.json.JSONArray;
import org.json.JSONObject;
import org.kohsuke.MetaInfServices;

import com.goodgame.profiling.commons.systems.net.jsonserver.Command;
import com.goodgame.profiling.graphite_bifroest.commands.submetrics.QueryParser;
import com.goodgame.profiling.graphite_bifroest.systems.prefixtree.EnvironmentWithPrefixTree;

@MetaInfServices
public class GetSubMetricsCommand< E extends EnvironmentWithPrefixTree > implements Command<E> {

    private static final Logger log = LogManager.getLogger();

    private static final Marker FAILED_QUERY = MarkerManager.getMarker( "FAILED_QUERY" );

    @Override
    public List<Class<? super E>> getRequiredEnvironments() {
        return Collections.<Class<? super E>> singletonList( EnvironmentWithPrefixTree.class );
    }

    @Override
    public String getJSONCommand() {
        return "get-sub-metrics";
    }

    @Override
    public List<Pair<String, Boolean>> getParameters() {
        return Collections.<Pair<String, Boolean>> singletonList( new ImmutablePair<>( "query", true ) );
    }

    @Override
    public JSONObject execute( JSONObject input, E environment ) {

        String query = input.getString( "query" );

        List<String> parseResults;
        try {
            parseResults = ( new QueryParser( query ) ).parse();
        } catch ( IllegalStateException e ) {
            log.warn( FAILED_QUERY, e.getMessage() + " while parsing <" + query + ">" );
            parseResults = Collections.emptyList();
        }

        List<Pair<String, Boolean>> results;
        results = new ArrayList<>();

        for ( String parseResult : parseResults ) {
            List<Pair<String, Boolean>> prefixes = environment.getTree().getPrefixesMatching( parseResult );
            log.trace( prefixes );
            results.addAll( prefixes );
        }

        JSONObject retval = new JSONObject();
        JSONArray resultsArray = new JSONArray();
        for ( Pair<String, Boolean> result : results ) {
            JSONObject oneResult = new JSONObject();
            oneResult.put( "path", result.getLeft() );
            oneResult.put( "isLeaf", result.getRight() );
            resultsArray.put( oneResult );
        }
        retval.put( "results", resultsArray );

        return retval;
    }

}
