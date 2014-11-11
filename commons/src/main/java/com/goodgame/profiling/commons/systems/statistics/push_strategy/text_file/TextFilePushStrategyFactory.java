package com.goodgame.profiling.commons.systems.statistics.push_strategy.text_file;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.json.JSONObject;
import org.kohsuke.MetaInfServices;

import com.goodgame.profiling.commons.systems.cron.EnvironmentWithTaskRunner;
import com.goodgame.profiling.commons.systems.statistics.EnvironmentWithStatisticsGatherer;
import com.goodgame.profiling.commons.systems.statistics.push_strategy.StatisticsPushStrategies;
import com.goodgame.profiling.commons.systems.statistics.push_strategy.StatisticsPushStrategy;
import com.goodgame.profiling.commons.systems.statistics.push_strategy.StatisticsPushStrategyFactory;
import com.goodgame.profiling.commons.systems.statistics.push_strategy.with_task.StatisticsPushStrategyWithTask;

@MetaInfServices
public class TextFilePushStrategyFactory<E extends EnvironmentWithStatisticsGatherer & EnvironmentWithTaskRunner> implements StatisticsPushStrategyFactory<E> {
    @Override
    public String handledType() {
        return "text-file";
    }

    @Override
    public StatisticsPushStrategy create( E environment, JSONObject config ) {
        Path path = Paths.get( config.getString( "path" ) );

        StatisticsPushStrategyWithTask<E> result =  new TextFilePushStrategy<E>( environment, path );
        StatisticsPushStrategies.<E>enablePeriodicPush( environment, result, config );
        return result;
    }
}
