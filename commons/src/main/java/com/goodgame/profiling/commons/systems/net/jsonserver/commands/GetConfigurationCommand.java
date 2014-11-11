package com.goodgame.profiling.commons.systems.net.jsonserver.commands;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONObject;
import org.kohsuke.MetaInfServices;

import com.goodgame.profiling.commons.systems.configuration.EnvironmentWithJSONConfiguration;
import com.goodgame.profiling.commons.systems.net.jsonserver.Command;

@MetaInfServices
public class GetConfigurationCommand< E extends EnvironmentWithJSONConfiguration > implements Command<E> {

    @Override
    public List<Class<? super E>> getRequiredEnvironments() {
        return Arrays.<Class<? super E>> asList( EnvironmentWithJSONConfiguration.class );
    }

    @Override
    public String getJSONCommand() {
        return "get-configuration";
    }

    @Override
    public List<Pair<String, Boolean>> getParameters() {
        return Collections.emptyList();
    }

    @Override
    public JSONObject execute( JSONObject input, E environment ) {
        return environment.getConfiguration();
    }

}
