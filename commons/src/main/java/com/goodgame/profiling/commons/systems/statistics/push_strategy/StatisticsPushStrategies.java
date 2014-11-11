package com.goodgame.profiling.commons.systems.statistics.push_strategy;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.goodgame.profiling.commons.statistics.WriteToStorageEvent;
import com.goodgame.profiling.commons.statistics.eventbus.EventBusManager;
import com.goodgame.profiling.commons.statistics.storage.MetricStorage;
import com.goodgame.profiling.commons.statistics.storage.TrieMetricStorage;
import com.goodgame.profiling.commons.statistics.units.parse.DurationParser;
import com.goodgame.profiling.commons.systems.cron.EnvironmentWithTaskRunner;
import com.goodgame.profiling.commons.systems.cron.TaskRunner.TaskID;
import com.goodgame.profiling.commons.systems.statistics.EnvironmentWithStatisticsGatherer;
import com.goodgame.profiling.commons.systems.statistics.push_strategy.with_task.StatisticsPushStrategyWithTask;

public final class StatisticsPushStrategies {
    private static final Logger log = LogManager.getLogger();

    private StatisticsPushStrategies() {
        // utility class
    }

    public static <E extends EnvironmentWithStatisticsGatherer&EnvironmentWithTaskRunner> void enablePeriodicPush( E environment, StatisticsPushStrategyWithTask<E> strategy, JSONObject config ) {
        String nameOfSubMetric = config.getString( "base" );
        Duration each = ( new DurationParser() ).parse( config.getString( "each" ) );

        enablePeriodicPush( environment, strategy, nameOfSubMetric, each );
    }

    public static <E extends EnvironmentWithStatisticsGatherer&EnvironmentWithTaskRunner> void enablePeriodicPush( E environment, StatisticsPushStrategyWithTask<E> strategy, String nameOfSubMetric, Duration each) {
        TaskID taskId = environment.taskRunner().runRepeated( ( ) -> writeMetrics( strategy, nameOfSubMetric ),
                                                              "StatisticsWriter",
                                                              Duration.ZERO, each,
                                                              false );
        strategy.setTaskId( taskId );
    }
    
    public static void collectMetrics( MetricStorage storage ) {
        EventBusManager.synchronousFire( new WriteToStorageEvent( Clock.systemUTC(), storage) );
    }
    
    public static void writeMetrics( StatisticsPushStrategy strategy, String nameOfSubMetric ) {
        final TrieMetricStorage storage = new TrieMetricStorage();
        final MetricStorage storageForGatherers = storage.getSubStorageCalled( nameOfSubMetric );
        collectMetrics( storageForGatherers );

        try {
            strategy.pushAll( storage.getAll() );
        } catch( IOException e ) {
            log.warn( "Exception while writing Metrics", e );
        }
    }

}
