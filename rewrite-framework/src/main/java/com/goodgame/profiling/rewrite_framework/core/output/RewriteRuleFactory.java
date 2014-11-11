package com.goodgame.profiling.rewrite_framework.core.output;

import org.json.JSONObject;

import com.goodgame.profiling.commons.boot.interfaces.Environment;

public interface RewriteRuleFactory<E extends Environment, I> {
	Class<?> handledInput();
	RewriteRule<I> create( E env, JSONObject config );
}
