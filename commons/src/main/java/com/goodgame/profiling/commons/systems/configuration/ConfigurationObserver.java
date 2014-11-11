package com.goodgame.profiling.commons.systems.configuration;

import org.json.JSONObject;

public interface ConfigurationObserver {
	void handleNewConfig( JSONObject conf );
}
