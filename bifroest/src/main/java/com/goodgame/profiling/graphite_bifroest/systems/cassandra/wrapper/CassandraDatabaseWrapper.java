package com.goodgame.profiling.graphite_bifroest.systems.cassandra.wrapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.goodgame.profiling.commons.model.Interval;
import com.goodgame.profiling.commons.model.Metric;
import com.goodgame.profiling.graphite_bifroest.systems.cassandra.CassandraAccessLayer;
import com.goodgame.profiling.graphite_bifroest.systems.cassandra.CassandraDatabase;
import com.goodgame.profiling.graphite_bifroest.systems.prefixtree.PrefixTree;
import com.goodgame.profiling.graphite_retentions.RetentionConfiguration;
import com.goodgame.profiling.graphite_retentions.RetentionLevel;
import com.goodgame.profiling.graphite_retentions.RetentionStrategy;
import com.goodgame.profiling.graphite_retentions.RetentionTable;

public class CassandraDatabaseWrapper implements CassandraAccessLayer {
    private static final Logger log = LogManager.getLogger();

    private final CassandraDatabase database;
    private final RetentionConfiguration retentionConfig;

    public CassandraDatabaseWrapper( CassandraDatabase database, RetentionConfiguration retentionconfig ) {
        this.database = database;
        this.retentionConfig = retentionconfig;
    }

    @Override
    public Collection<String> loadMetricNames() {

        Map<String, List<RetentionTable>> retentionTables = createRetentionTables();
        Set<String> dataSet = new HashSet<>();

        for ( Map.Entry<String, List<RetentionTable>> retentionStrategy : retentionTables.entrySet() ) {

            Collections.sort( retentionStrategy.getValue() );
            Iterator<RetentionTable> tableIterator = retentionStrategy.getValue().iterator();
            int numBlocks = retentionConfig.getStrategyForName( retentionStrategy.getKey() ).nameRetentionBlocks();
            long now = System.currentTimeMillis() / 1000;

            for ( int i = 0; i < numBlocks; i++ ) {

                if ( tableIterator.hasNext() ) {
                    RetentionTable table = tableIterator.next();

                    // If we miss a table
                    if ( table.level().indexOf( now ) - i != table.block() ) {
                        i += table.level().indexOf( now ) - i - table.block();

                        if ( i >= numBlocks ) {
                            break;
                        }
                    }

                    for ( String name : database.loadMetricNames( table ) ) {
                        dataSet.add( name );
                    }
                }
            }
        }

        return dataSet;
    }

    @Override
    public Pair<PrefixTree, Integer> loadMostRecentTimestamps() {
        Map<String, List<RetentionTable>> retentionTables = createRetentionTables();
        PrefixTree tree = new PrefixTree();
        int numberOfMetricNames = 0;

        Collection<String> remainingMetricNames = loadMetricNames();

        for ( Map.Entry<String, List<RetentionTable>> retentionStrategy : retentionTables.entrySet() ) {

            Collections.sort( retentionStrategy.getValue() );
            Iterator<RetentionTable> tableIterator = retentionStrategy.getValue().iterator();
            int numBlocks = retentionConfig.getStrategyForName( retentionStrategy.getKey() ).nameRetentionBlocks();
            long now = System.currentTimeMillis() / 1000;

            for ( int i = 0; i < numBlocks; i++ ) {
                if ( tableIterator.hasNext() ) {
                    RetentionTable table = tableIterator.next();

                    // If we miss a table
                    if ( table.level().indexOf( now ) - i != table.block() ) {
                        i += table.level().indexOf( now ) - i - table.block();

                        if ( i >= numBlocks ) {
                            break;
                        }
                    }

                    Iterator<String> it = remainingMetricNames.iterator();
                    while (it.hasNext()) {
                        String metricName = it.next();
                        Long timestamp = database.loadHighestTimestamp( table, metricName );
                        if (timestamp != null) {
                            it.remove();
                            tree.addEntry( metricName, timestamp );
                            numberOfMetricNames++;
                            if (remainingMetricNames.size() % 100 == 0) {
                                log.trace("Tree Rebuild - Remaining metrics: " + remainingMetricNames.size());
                            }
                        }
                    }
                }
            }
        }

        return Pair.<PrefixTree, Integer>of(tree, numberOfMetricNames);
    }

    private Map<String, List<RetentionTable>> createRetentionTables() {
        Map<String, List<RetentionTable>> retentionTables = new HashMap<>();

        for ( RetentionTable table : database.loadTables() ) {
            if ( retentionTables.containsKey( table.strategy().name() ) ) {
                retentionTables.get( table.strategy().name() ).add( table );
            } else {
                List<RetentionTable> newTableList = new ArrayList<>();
                newTableList.add( table );
                retentionTables.put( table.strategy().name(), newTableList );
            }
        }

        return retentionTables;
    }

    @Override
    public Iterable<Metric> loadMetrics( final String name, final Interval interval ) {
        RetentionStrategy strategy = retentionConfig.findStrategyForMetric( name );

        final SortedMap<RetentionLevel, List<RetentionTable>> levels = new TreeMap<>();

        for ( RetentionTable table : database.loadTables() ) {
            if ( table.strategy().equals( strategy ) ) {
                if ( !levels.containsKey( table.level() ) ) {
                    levels.put( table.level(), new ArrayList<RetentionTable>() );
                }
                levels.get( table.level() ).add( table );
            }
        }
        for ( List<RetentionTable> tables : levels.values() ) {
            Collections.sort( tables );
        }

        return new Iterable<Metric>() {

            @Override
            public Iterator<Metric> iterator() {
                return MetricIterator.create( database, name, interval, levels.entrySet().iterator() );
            }

        };
    }
}
