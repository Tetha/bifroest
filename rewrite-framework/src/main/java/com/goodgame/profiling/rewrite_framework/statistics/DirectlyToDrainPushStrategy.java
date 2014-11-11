package com.goodgame.profiling.rewrite_framework.statistics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.goodgame.profiling.commons.model.Metric;
import com.goodgame.profiling.commons.systems.configuration.EnvironmentWithJSONConfiguration;
import com.goodgame.profiling.commons.systems.cron.EnvironmentWithTaskRunner;
import com.goodgame.profiling.commons.systems.statistics.push_strategy.with_task.StatisticsPushStrategyWithTask;
import com.goodgame.profiling.rewrite_framework.core.drain.Drain;
import com.goodgame.profiling.rewrite_framework.core.drain.DrainCreator;

public class DirectlyToDrainPushStrategy<E extends EnvironmentWithJSONConfiguration & EnvironmentWithTaskRunner> extends StatisticsPushStrategyWithTask<E> {
    private static final Logger log = LogManager.getLogger();

    private final Drain drain;

    public DirectlyToDrainPushStrategy( E environment, JSONObject config ) {
        super( environment );
        drain = new DrainCreator<>().loadConfiguration( environment, config );
    }

    @Override
    public void pushAll( Collection<Metric> metrics ) throws IOException {
        drain.output( new ArrayList<>( metrics ) );
    }

    @Override
    public void closeAfterTaskStopped() throws IOException {
        try {
            drain.flushRemainingBuffers();
        } catch( IOException e ) {
            log.warn( "Exception while flushing buffers", e );
        }
        try {
            drain.close();
        } catch( IOException e ) {
            log.warn( "Exception while closing drains", e );
        }
    }
}
