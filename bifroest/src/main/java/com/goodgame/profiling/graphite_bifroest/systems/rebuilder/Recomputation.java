package com.goodgame.profiling.graphite_bifroest.systems.rebuilder;

import java.time.Clock;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.goodgame.profiling.commons.statistics.eventbus.EventBusManager;
import com.goodgame.profiling.graphite_bifroest.systems.cassandra.EnvironmentWithCassandra;
import com.goodgame.profiling.graphite_bifroest.systems.prefixtree.EnvironmentWithMutablePrefixTree;
import com.goodgame.profiling.graphite_bifroest.systems.prefixtree.PrefixTree;
import com.goodgame.profiling.graphite_bifroest.systems.rebuilder.statistics.RebuildFinishedEvent;
import com.goodgame.profiling.graphite_bifroest.systems.rebuilder.statistics.RebuildStartedEvent;

public class Recomputation< E extends EnvironmentWithCassandra & EnvironmentWithMutablePrefixTree > implements Runnable {
    private final Logger log = LogManager.getLogger();

    private final E environment;

    public Recomputation( E environment ) {
        this.environment = environment;
    }

    @Override
    public void run() {
        log.debug( "Started recomputation!" );
        try {
            rebuild();
        } catch ( Exception e ) {
            log.warn( "An unexpected exception occured during recomputation", e );
        }
    }

    protected void rebuild() {
        EventBusManager.fire( new RebuildStartedEvent( Clock.systemUTC() ) );
        Pair<PrefixTree, Integer> pair = environment.cassandraAccessLayer().loadMostRecentTimestamps();

        environment.setTree( pair.getLeft() );
        EventBusManager.fire( new RebuildFinishedEvent( Clock.systemUTC(), pair.getRight() ) );
    }
}
