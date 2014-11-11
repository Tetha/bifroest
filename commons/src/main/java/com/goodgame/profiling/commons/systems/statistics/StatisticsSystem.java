package com.goodgame.profiling.commons.systems.statistics;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.goodgame.profiling.commons.boot.interfaces.Subsystem;
import com.goodgame.profiling.commons.statistics.eventbus.EventBusManager;
import com.goodgame.profiling.commons.statistics.eventbus.EventBus;
import com.goodgame.profiling.commons.statistics.eventbus.disruptor.DisruptorEventBus;
import com.goodgame.profiling.commons.statistics.gathering.CompositeStatisticGatherer;
import com.goodgame.profiling.commons.statistics.gathering.StatisticGatherer;
import com.goodgame.profiling.commons.systems.SystemIdentifiers;
import com.goodgame.profiling.commons.systems.configuration.EnvironmentWithJSONConfiguration;
import com.goodgame.profiling.commons.systems.cron.EnvironmentWithTaskRunner;
import com.goodgame.profiling.commons.systems.statistics.push_strategy.StatisticsPushStrategy;
import com.goodgame.profiling.commons.systems.statistics.push_strategy.StatisticsPushStrategyCreator;

public class StatisticsSystem<E extends EnvironmentWithJSONConfiguration & EnvironmentWithMutableStatisticsGatherer & EnvironmentWithTaskRunner> implements
        Subsystem<E> {

    private static final Logger log = LogManager.getLogger();

    private StatisticsPushStrategy pushStrategy;

    @Override
    public String getSystemIdentifier() {
        return SystemIdentifiers.STATISTICS;
    }

    @Override
    public Collection<String> getRequiredSystems() {
        return Arrays.asList( SystemIdentifiers.LOGGING, SystemIdentifiers.CONFIGURATION, SystemIdentifiers.CRON );
    }

    @Override
    public Collection<String> getWeaklyRequiredSystems() {
        return Arrays.asList( SystemIdentifiers.PERSISTENT_DRAINS );
    }


    @Override
    public void boot( E environment ) {
        JSONObject config = environment.getConfiguration().getJSONObject( "statistics" );

        JSONObject pushConfig = config.getJSONObject( "metric-push" );

        JSONObject eventBusConfig = config.getJSONObject( "eventbus" );
        EventBus bus = new DisruptorEventBus( eventBusConfig.getInt( "handler-count" ),
                                              eventBusConfig.getInt( "size-exponent" ) );

        // must be done before StatisticGatherer.init
        EventBusManager.setEventBus( bus );

        StatisticGatherer composite = new CompositeStatisticGatherer();
        composite.init();
        environment.setStatisticGatherer( composite );


        pushStrategy = new StatisticsPushStrategyCreator<>().create( environment, pushConfig );
    }

    @Override
    public void shutdown( E environment ) {
        try {
            pushStrategy.close();
        } catch( IOException e ) {
            log.warn( "Exception while closing push strategy", e );
        }
        pushStrategy = null;

        try {
            EventBusManager.getEventBus().shutdown();
        } catch( InterruptedException e ) {
            log.warn( "Interrupted while shutting down eventBus", e );
        }
    }
}
