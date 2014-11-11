package com.goodgame.profiling.commons.systems.statistics.push_strategy;

import java.util.ServiceLoader;

import org.json.JSONObject;

import com.goodgame.profiling.commons.boot.interfaces.Environment;

public class StatisticsPushStrategyCreator<E extends Environment> {
    @SuppressWarnings( "unchecked" )
    public StatisticsPushStrategy create( E environment, JSONObject config ) {
        for ( StatisticsPushStrategyFactory<E> factory : ServiceLoader.load( StatisticsPushStrategyFactory.class ) ) {
            if( factory.handledType().equals( config.getString( "type" ) ) ) {
                return factory.create( environment, config );
            }
        }
        
        throw new IllegalStateException( "No matching StatisticsPushStrategy found!" );
    }
}
