package com.goodgame.profiling.commons.systems.statistics.push_strategy;

import org.json.JSONObject;

import com.goodgame.profiling.commons.boot.interfaces.Environment;

public interface StatisticsPushStrategyFactory<E extends Environment> {
    StatisticsPushStrategy create( E environment, JSONObject config );
    String handledType();
}
