package com.goodgame.profiling.rewrite_framework.drain.persistent.cassandra;

import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;
import org.kohsuke.MetaInfServices;

import com.goodgame.profiling.commons.util.json.JSONUtils;
import com.goodgame.profiling.graphite_retentions.bootloader.EnvironmentWithRetentionStrategy;
import com.goodgame.profiling.rewrite_framework.drain.persistent.PersistentDrainFactory;

@MetaInfServices
public class PersistentCassandraDrainFactory<E extends EnvironmentWithRetentionStrategy> implements
        PersistentDrainFactory<E, PersistentCassandraDrain> {

    @Override
    public List<Class<? super E>> getRequiredEnvironments() {
        return Arrays.<Class<? super E>> asList( EnvironmentWithRetentionStrategy.class );
    }

    @Override
    public String handledType() {
        return "cassandra";
    }

    @Override
    public PersistentCassandraDrain create( E environment, JSONObject config ) {
        return new PersistentCassandraDrain(
                JSONUtils.getWithDefault( config, "username", (String) null ),
                JSONUtils.getWithDefault( config, "password", (String) null ),
                JSONUtils.getStringArray( "seeds", config ),
                config.getString( "keyspace" ),
                environment.retentions() );
    }

}
