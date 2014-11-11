package com.goodgame.profiling.rewrite_framework.statistics;

import org.json.JSONObject;
import org.kohsuke.MetaInfServices;

import com.goodgame.profiling.commons.systems.configuration.EnvironmentWithJSONConfiguration;
import com.goodgame.profiling.commons.systems.cron.EnvironmentWithTaskRunner;
import com.goodgame.profiling.commons.systems.statistics.EnvironmentWithStatisticsGatherer;
import com.goodgame.profiling.commons.systems.statistics.push_strategy.StatisticsPushStrategies;
import com.goodgame.profiling.commons.systems.statistics.push_strategy.StatisticsPushStrategy;
import com.goodgame.profiling.commons.systems.statistics.push_strategy.StatisticsPushStrategyFactory;
import com.goodgame.profiling.commons.systems.statistics.push_strategy.with_task.StatisticsPushStrategyWithTask;

@MetaInfServices
public class DirectlyToDrainPushStrategyFactory<E extends EnvironmentWithJSONConfiguration & EnvironmentWithStatisticsGatherer & EnvironmentWithTaskRunner> implements StatisticsPushStrategyFactory<E> {
    @Override
    public StatisticsPushStrategy create( E environment, JSONObject config ) {
        StatisticsPushStrategyWithTask<E> result = new DirectlyToDrainPushStrategy<E>( environment, config );
        StatisticsPushStrategies.<E>enablePeriodicPush( environment, result, config );
        return result;
    }

    @Override
    public String handledType() {
        return "internal";
    }
}
