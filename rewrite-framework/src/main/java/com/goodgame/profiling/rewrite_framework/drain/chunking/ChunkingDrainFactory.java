package com.goodgame.profiling.rewrite_framework.drain.chunking;

import java.util.Collections;
import java.util.List;

import org.json.JSONObject;
import org.kohsuke.MetaInfServices;

import com.goodgame.profiling.commons.boot.interfaces.Environment;
import com.goodgame.profiling.rewrite_framework.core.drain.Drain;
import com.goodgame.profiling.rewrite_framework.core.drain.DrainWrapperFactory;

@MetaInfServices
public class ChunkingDrainFactory<E extends Environment> implements DrainWrapperFactory<E> {
    @Override
    public String handledType() {
        return "chunked";
    }

    @Override
    public List<Class<? super E>> getRequiredEnvironments() {
        return Collections.emptyList();
    }

    @Override
    public Drain wrap( E environment, Drain inner, JSONObject subconfiguration ) {
        return new ChunkingDrain( inner, subconfiguration.getInt( "chunksize" ) );
    }
}
