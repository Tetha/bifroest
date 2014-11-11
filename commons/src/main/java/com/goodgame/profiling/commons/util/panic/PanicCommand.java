package com.goodgame.profiling.commons.util.panic;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONObject;
import org.kohsuke.MetaInfServices;

import com.goodgame.profiling.commons.boot.interfaces.Environment;
import com.goodgame.profiling.commons.systems.net.jsonserver.Command;

@MetaInfServices
public class PanicCommand<E extends Environment> implements Command<E> {

    @Override
    public List<Class<? super E>> getRequiredEnvironments() {
        return Arrays.asList( );
    }

    @Override
    public String getJSONCommand() {
        return "panic";
    }

    @Override
    public List<Pair<String, Boolean>> getParameters() {
        return Collections.emptyList();
    }

    @Override
    public JSONObject execute( JSONObject input, E environment ) {
        ProfilingPanic.INSTANCE.panic();
        
        return new JSONObject();
    }
}
