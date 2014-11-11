package com.goodgame.profiling.commons.systems.statistics.push_strategy.plaintext_carbon;

import org.json.JSONObject;
import org.kohsuke.MetaInfServices;

import com.goodgame.profiling.commons.systems.statistics.push_strategy.StatisticsPushStrategies;
import com.goodgame.profiling.commons.systems.statistics.push_strategy.StatisticsPushStrategy;
import com.goodgame.profiling.commons.systems.statistics.push_strategy.StatisticsPushStrategyFactory;
import com.goodgame.profiling.commons.systems.statistics.push_strategy.with_task.StatisticsPushStrategyWithTask;
import com.goodgame.profiling.commons.systems.statistics.EnvironmentWithStatisticsGatherer;
import com.goodgame.profiling.commons.systems.cron.EnvironmentWithTaskRunner;

@MetaInfServices
public class PlainTextCarbonPushStrategyFactory<E extends EnvironmentWithTaskRunner&EnvironmentWithStatisticsGatherer> implements StatisticsPushStrategyFactory<E> {
    public StatisticsPushStrategy create( E environment, JSONObject config ) {
        String host = config.getString( "host" );
        int port = config.getInt( "port" );
        
        StatisticsPushStrategyWithTask<E> result = new PlainTextCarbonPushStrategy<E>( environment, host, port );
        StatisticsPushStrategies.<E>enablePeriodicPush( environment, result, config );
        return result;
    }
    
    public String handledType() {
        return "plain-text-carbon";
    }
}
