package com.goodgame.profiling.commons.systems.net.jsonserver;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONObject;

import com.goodgame.profiling.commons.boot.interfaces.Environment;

public interface Command< E extends Environment > {

    List<Class<? super E>> getRequiredEnvironments();

    String getJSONCommand();

    List<Pair<String, Boolean>> getParameters();

    JSONObject execute( JSONObject input, E environment );

}
