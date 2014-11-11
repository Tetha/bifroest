package com.goodgame.profiling.commons.systems.statistics.push_strategy.composite;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.kohsuke.MetaInfServices;

import com.goodgame.profiling.commons.boot.interfaces.Environment;
import com.goodgame.profiling.commons.systems.statistics.push_strategy.StatisticsPushStrategy;
import com.goodgame.profiling.commons.systems.statistics.push_strategy.StatisticsPushStrategyCreator;
import com.goodgame.profiling.commons.systems.statistics.push_strategy.StatisticsPushStrategyFactory;

@MetaInfServices
public class CompositePushStrategyFactory<E extends Environment> implements StatisticsPushStrategyFactory<E> {
    @Override
    public String handledType() {
        return "composite";
    }

    @Override
    public StatisticsPushStrategy create( E environment, JSONObject config ) {
        JSONArray innerConfigs = config.getJSONArray( "inners" );
        List<StatisticsPushStrategy> inners = new ArrayList<>();

        for( int i = 0; i < innerConfigs.length(); i++ ) {
            inners.add( new StatisticsPushStrategyCreator<>().create( environment, innerConfigs.getJSONObject( i ) ) );
        }

        return new CompositePushStrategy( inners );
    }
}
