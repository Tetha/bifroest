package com.goodgame.profiling.commons.systems.configuration;

import org.json.JSONObject;

import com.goodgame.profiling.commons.boot.interfaces.Environment;

public interface EnvironmentWithJSONConfiguration extends Environment {

	JSONObject getConfiguration();

	JSONConfigurationLoader getConfigurationLoader();

}
