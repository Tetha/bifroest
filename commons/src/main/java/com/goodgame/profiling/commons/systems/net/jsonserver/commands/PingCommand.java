package com.goodgame.profiling.commons.systems.net.jsonserver.commands;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONObject;
import org.kohsuke.MetaInfServices;

import com.goodgame.profiling.commons.boot.interfaces.Environment;
import com.goodgame.profiling.commons.systems.net.jsonserver.Command;

@MetaInfServices
public class PingCommand implements Command<Environment> {

    @Override
    public List<Class<? super Environment>> getRequiredEnvironments() {
        return Collections.emptyList();
    }

    @Override
    public String getJSONCommand() {
        return "ping";
    }

    @Override
    public List<Pair<String, Boolean>> getParameters() {
        return Collections.emptyList();
    }

    @Override
    public JSONObject execute( JSONObject input, Environment environment ) {
        return new JSONObject().put( "ping", "pong" );
    }

}
