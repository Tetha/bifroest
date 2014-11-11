package com.goodgame.profiling.rewrite_framework.core.source;

import org.json.JSONObject;

import com.goodgame.profiling.commons.boot.interfaces.Environment;

public interface SourceSetFactory<E extends Environment, U> {
	Class<U> handledUnit();
	String handledType();

	SourceSet<U> create( E env, JSONObject config );
}
