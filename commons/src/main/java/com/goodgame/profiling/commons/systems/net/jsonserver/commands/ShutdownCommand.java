package com.goodgame.profiling.commons.systems.net.jsonserver.commands;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONObject;
import org.kohsuke.MetaInfServices;

import com.goodgame.profiling.commons.boot.EnvironmentWithInit;
import com.goodgame.profiling.commons.systems.net.jsonserver.Command;

@MetaInfServices
public class ShutdownCommand< E extends EnvironmentWithInit > implements Command<E> {

    @Override
    public List<Class<? super E>> getRequiredEnvironments() {
        return Arrays.<Class<? super E>> asList( EnvironmentWithInit.class );
    }

    @Override
    public String getJSONCommand() {
        return "shutdown";
    }

    @Override
    public List<Pair<String, Boolean>> getParameters() {
        return Collections.emptyList();
    }

    @Override
    public JSONObject execute( JSONObject input, final E environment ) {
        new Thread() {

            @Override
            public synchronized void run() {
                environment.initD().shutdown();
            }

        }.start();
        return new JSONObject();
    }

}
