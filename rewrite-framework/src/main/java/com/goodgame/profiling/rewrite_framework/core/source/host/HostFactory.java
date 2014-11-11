package com.goodgame.profiling.rewrite_framework.core.source.host;

import java.util.Collection;

import org.json.JSONObject;

public interface HostFactory {
	String handledType();
	Collection<String> create( JSONObject config );
}
