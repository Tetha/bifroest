package com.goodgame.profiling.rewrite_framework.core;

import org.json.JSONObject;

import com.goodgame.profiling.commons.boot.interfaces.Environment;

public interface ConfigurationFactory< E extends Environment, T > {

    T loadConfiguration( E env, JSONObject conf );

}
