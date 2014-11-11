package com.goodgame.profiling.commons.systems.statistics.push_strategy.composite;

import java.io.IOException;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.goodgame.profiling.commons.model.Metric;
import com.goodgame.profiling.commons.systems.statistics.push_strategy.StatisticsPushStrategy;

public class CompositePushStrategy implements StatisticsPushStrategy {
    private static final Logger log = LogManager.getLogger();

    private final Collection<StatisticsPushStrategy> inners;

    public CompositePushStrategy( Collection<StatisticsPushStrategy> inners ) {
        this.inners = inners;
    }

    @Override
    public void pushAll( Collection<Metric> metrics ) throws IOException {
        for( StatisticsPushStrategy inner : inners ) {
            try {
                inner.pushAll( metrics );
            } catch( Exception e ) {
                log.warn( "Exception while pushing metrics", e );
            }
        }
    }

    @Override
    public void close() throws IOException {
        for( StatisticsPushStrategy inner : inners ) {
            try {
                inner.close();
            } catch( Exception e ) {
                log.warn( "Exception while closing inner StatisticsPushStrategy {}", inner, e );
            }
        }
    }
}
