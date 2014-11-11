package com.goodgame.profiling.graphite_bifroest.commands;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.kohsuke.MetaInfServices;

import com.goodgame.profiling.commons.model.Metric;
import com.goodgame.profiling.commons.systems.net.jsonserver.Command;
import com.goodgame.profiling.graphite_bifroest.metric_cache.EnvironmentWithMetricCache;
import com.goodgame.profiling.graphite_bifroest.systems.prefixtree.EnvironmentWithPrefixTree;

@MetaInfServices
public class IncludeMetrics<E extends EnvironmentWithPrefixTree & EnvironmentWithMetricCache> implements Command<E> {
    @Override
    public List<Class<? super E>> getRequiredEnvironments() {
        return Arrays.<Class<? super E>> asList( EnvironmentWithPrefixTree.class, EnvironmentWithMetricCache.class );
    }

    @Override
    public String getJSONCommand() {
        return "include-metrics";
    }

    @Override
    public List<Pair<String, Boolean>> getParameters() {
        return Collections.<Pair<String, Boolean>> singletonList( new ImmutablePair<>( "metrics", true ) );
    }

    @Override
    public JSONObject execute( JSONObject input, E environment ) {
        JSONArray metrics = input.getJSONArray( "metrics" );

        for ( int i = 0; i < metrics.length(); i++ ) {
            JSONObject metric = metrics.getJSONObject( i );

            String metricName = metric.getString( "name" );
            long timestamp = metric.getLong( "timestamp" );
            double value = metric.getDouble( "value" );

            environment.getTree().addEntry( metricName, timestamp );
            environment.metricCache().put( new Metric( metricName, timestamp, value ) );
        }

        return new JSONObject();
    }
}
