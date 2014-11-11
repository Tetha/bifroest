package com.goodgame.profiling.graphite_aggregator.systems.cassandra;

import java.util.Arrays;
import java.util.Collection;

import org.json.JSONObject;

import com.goodgame.profiling.commons.boot.interfaces.Subsystem;
import com.goodgame.profiling.commons.systems.SystemIdentifiers;
import com.goodgame.profiling.commons.systems.configuration.EnvironmentWithJSONConfiguration;
import com.goodgame.profiling.commons.util.json.JSONUtils;
import com.goodgame.profiling.graphite_aggregator.systems.AggregatorIdentifiers;
import com.goodgame.profiling.graphite_retentions.bootloader.EnvironmentWithRetentionStrategy;

public class CassandraSystem< E extends EnvironmentWithJSONConfiguration & EnvironmentWithRetentionStrategy & EnvironmentWithMutableCassandra > implements
        Subsystem<E> {

    private CassandraAccessLayer cassandra;

    @Override
    public String getSystemIdentifier() {
        return AggregatorIdentifiers.CASSANDRA;
    }

    @Override
    public Collection<String> getRequiredSystems() {
        return Arrays.asList( SystemIdentifiers.LOGGING, SystemIdentifiers.CONFIGURATION, SystemIdentifiers.RETENTION, SystemIdentifiers.STATISTICS );
    }

    @Override
    public void boot( E environment ) throws Exception {
        JSONObject config = environment.getConfiguration().getJSONObject( "cassandra" );
        String username = config.optString( "username", null );
        String password = config.optString( "password", null );
        String keyspace = config.getString( "keyspace" );
        String[] seeds = JSONUtils.getStringArray( "seeds", config );
        boolean dryRun = config.optBoolean( "dry-run", false );
        cassandra = new CassandraAccessLayer( username, password, keyspace, seeds, environment.retentions(), dryRun );

        cassandra.open();
        environment.setCassandraAccessLayer( cassandra );
    }

    @Override
    public void shutdown( E environment ) {
        cassandra.close();
    }

}
