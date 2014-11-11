package com.goodgame.profiling.rewrite_framework.systems.gatherer.optimization;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;
import org.junit.Test;

public class ThreadCountOptimizerTest {
    private class AlwaysRunStrategy implements ShouldRunOptimizerStrategy {
        @Override
        public boolean shouldRun() {
            return true;
        }
    }

    private class ModifiableRunStrategy implements ShouldRunOptimizerStrategy {
        public boolean shouldRun = false;

        @Override
        public boolean shouldRun() {
            return this.shouldRun;
        }
    }

    @Test
    public void testIncreaseThreadCount() {
        JSONObject config = new JSONObject();
        config.put( "repeat", 1 );
        config.put( "persistenceFile", "/dev/null" );
        config.put( "initialPoolSize", 2 );

        ThreadCountOptimizer subject = new ThreadCountOptimizer( config, new AlwaysRunStrategy() );

        Map<Integer, Integer> runtimes = new HashMap<>();
        runtimes.put(1, 5000);
        runtimes.put(2, 3000);
        runtimes.put(3, 2500);
        subject.recordRuntime(runtimes.get(subject.nextTreadCount()));
        subject.recordRuntime(runtimes.get(subject.nextTreadCount()));
        subject.recordRuntime(runtimes.get(subject.nextTreadCount()));
        assertEquals(3, subject.currentOptimalPoolSize());
    }

    @Test
    public void testKeepThreadCount() {
        JSONObject config = new JSONObject();
        config.put( "repeat", 1 );
        config.put( "persistenceFile", "/dev/null" );
        config.put( "initialPoolSize", 3 );

        ThreadCountOptimizer subject = new ThreadCountOptimizer( config, new AlwaysRunStrategy() );

        Map<Integer, Integer> runtimes = new HashMap<>();
        runtimes.put(2, 3000);
        runtimes.put(3, 2500);
        runtimes.put(4, 2600);
        subject.recordRuntime(runtimes.get(subject.nextTreadCount()));
        subject.recordRuntime(runtimes.get(subject.nextTreadCount()));
        subject.recordRuntime(runtimes.get(subject.nextTreadCount()));
        assertEquals(3, subject.currentOptimalPoolSize());
    }

    @Test
    public void testStartWithOneThread() {
        JSONObject config = new JSONObject();
        config.put( "repeat", 1 );
        config.put( "persistenceFile", "/dev/null" );
        config.put( "initialPoolSize", 1 );

        ThreadCountOptimizer subject = new ThreadCountOptimizer( config, new AlwaysRunStrategy() );

        Map<Integer, Integer> runtimes = new HashMap<>();
        runtimes.put(1, 5000);
        runtimes.put(2, 3000);
        subject.recordRuntime(runtimes.get(subject.nextTreadCount()));
        subject.recordRuntime(runtimes.get(subject.nextTreadCount()));
        subject.recordRuntime(runtimes.get(subject.nextTreadCount()));
        assertEquals(2, subject.currentOptimalPoolSize());
    }

    @Test
    public void honorsRunStrategy() {
        JSONObject config = new JSONObject();
        config.put( "repeat", 1 );
        config.put( "persistenceFile", "/dev/null" );
        config.put( "initialPoolSize", 2 );

        ModifiableRunStrategy strategy = new ModifiableRunStrategy();
        ThreadCountOptimizer subject = new ThreadCountOptimizer( config, strategy );
        strategy.shouldRun = false;

        Set<Integer> threadCounts = new HashSet<>();

        for( int i = 0; i < 10; i++ ) {
            threadCounts.add(subject.nextTreadCount());
            subject.recordRuntime(1000);
        }
        assertEquals(new HashSet<>(Arrays.asList(2)), threadCounts);

        strategy.shouldRun = true;
        threadCounts.clear();

        for( int i = 0; i < 3; i++ ) {
            threadCounts.add(subject.nextTreadCount());
            subject.recordRuntime(1000);
        }
        assertEquals(new HashSet<>(Arrays.asList(1,2,3)), threadCounts);
    }
}
