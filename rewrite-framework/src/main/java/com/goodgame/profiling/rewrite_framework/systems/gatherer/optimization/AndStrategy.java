package com.goodgame.profiling.rewrite_framework.systems.gatherer.optimization;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AndStrategy implements ShouldRunOptimizerStrategy {
    private static final Logger log = LogManager.getLogger();

    private ShouldRunOptimizerStrategy[] strategies;

    public AndStrategy(ShouldRunOptimizerStrategy...strategies) {
        this.strategies = strategies;
    }

    @Override
    public boolean shouldRun() {
        for (ShouldRunOptimizerStrategy strategy : strategies) {
            boolean shouldRun = strategy.shouldRun();
            log.trace( strategy.toString() + " " + shouldRun );
            if( !shouldRun ) {
                return false;
            }
        }
        return true;
    }
}
