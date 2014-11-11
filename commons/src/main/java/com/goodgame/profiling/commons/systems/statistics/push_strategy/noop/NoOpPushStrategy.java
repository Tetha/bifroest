package com.goodgame.profiling.commons.systems.statistics.push_strategy.noop;

import java.io.IOException;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.kohsuke.MetaInfServices;

import com.goodgame.profiling.commons.boot.interfaces.Environment;
import com.goodgame.profiling.commons.model.Metric;
import com.goodgame.profiling.commons.systems.statistics.push_strategy.StatisticsPushStrategy;
import com.goodgame.profiling.commons.systems.statistics.push_strategy.StatisticsPushStrategyFactory;

@MetaInfServices(StatisticsPushStrategyFactory.class)
public class NoOpPushStrategy<E extends Environment> implements StatisticsPushStrategy, StatisticsPushStrategyFactory<E> {
    private static final Logger log = LogManager.getLogger();
    
    @Override
    public StatisticsPushStrategy create( E environment, JSONObject config ) {
        return this;
    }
    
    public String handledType() {
        return "no-op";
    }

    @Override
    public void pushAll( Collection<Metric> metrics ) {
        log.debug( "Dropped metrics: {}", metrics );
    }

    @Override
    public void close() throws IOException {
        // Don't do anything
    }
}
