package com.goodgame.profiling.rewrite_framework.core;

import java.util.List;

import org.json.JSONObject;

import com.goodgame.profiling.commons.boot.interfaces.Environment;

public interface MultipleWrappingFactory<E extends Environment, T> {
    List<Class<? super E>> getRequiredEnvironments();
    public String handledType();
    public T wrap ( E environment, List<T> inners, JSONObject subconfiguration );
}
