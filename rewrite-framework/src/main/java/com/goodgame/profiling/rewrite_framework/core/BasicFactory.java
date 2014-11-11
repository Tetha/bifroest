package com.goodgame.profiling.rewrite_framework.core;

import java.util.List;

import org.json.JSONObject;

import com.goodgame.profiling.commons.boot.interfaces.Environment;

public interface BasicFactory<E extends Environment, T> {
	List<Class<? super E>> getRequiredEnvironments();
	public String handledType();
	public T create ( E environment, JSONObject subconfiguration );
}
