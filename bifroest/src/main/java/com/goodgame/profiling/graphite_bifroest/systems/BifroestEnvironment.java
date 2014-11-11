package com.goodgame.profiling.graphite_bifroest.systems;

import java.nio.file.Path;

import com.goodgame.profiling.commons.boot.InitD;
import com.goodgame.profiling.commons.systems.common.AbstractCommonEnvironment;
import com.goodgame.profiling.commons.systems.cron.EnvironmentWithMutableTaskRunner;
import com.goodgame.profiling.commons.systems.cron.TaskRunner;
import com.goodgame.profiling.graphite_bifroest.metric_cache.EnvironmentWithMutableMetricCache;
import com.goodgame.profiling.graphite_bifroest.metric_cache.MetricCache;
import com.goodgame.profiling.graphite_bifroest.systems.cassandra.CassandraAccessLayer;
import com.goodgame.profiling.graphite_bifroest.systems.cassandra.EnvironmentWithMutableCassandra;
import com.goodgame.profiling.graphite_bifroest.systems.prefixtree.EnvironmentWithMutablePrefixTree;
import com.goodgame.profiling.graphite_bifroest.systems.prefixtree.PrefixTree;
import com.goodgame.profiling.graphite_bifroest.systems.rebuilder.EnvironmentWithMutableTreeRebuilder;
import com.goodgame.profiling.graphite_bifroest.systems.rebuilder.TreeRebuilder;
import com.goodgame.profiling.graphite_retentions.RetentionConfiguration;
import com.goodgame.profiling.graphite_retentions.bootloader.EnvironmentWithMutableRetentionStrategy;

public class BifroestEnvironment extends AbstractCommonEnvironment implements
        EnvironmentWithMutableTaskRunner,
        EnvironmentWithMutableCassandra,
        EnvironmentWithMutablePrefixTree,
        EnvironmentWithMutableTreeRebuilder,
        EnvironmentWithMutableRetentionStrategy,
        EnvironmentWithMutableMetricCache
{
    private TaskRunner cron;
    private CassandraAccessLayer cassandra;
    private volatile PrefixTree tree; // TODO can we enforce this somewhere?
    private TreeRebuilder rebuilder;
    private RetentionConfiguration retentions;
    private MetricCache cache;

    public BifroestEnvironment( Path configPath, InitD init ) {
        super( configPath, init );
    }

    @Override
    public TaskRunner taskRunner() {
        return cron;
    }

    @Override
    public void setTaskRunner( TaskRunner taskRunner ) {
        this.cron = taskRunner;
    }

    @Override
    public CassandraAccessLayer cassandraAccessLayer() {
        return cassandra;
    }

    @Override
    public void setCassandraAccessLayer( CassandraAccessLayer cassandra ) {
        this.cassandra = cassandra;
    }

    @Override
    public PrefixTree getTree() {
        return tree;
    }

    @Override
    public void setTree( PrefixTree tree ) {
        this.tree = tree;
    }

    @Override
    public TreeRebuilder getRebuilder() {
        return rebuilder;
    }

    @Override
    public void setRebuilder( TreeRebuilder rebuilder ) {
        this.rebuilder = rebuilder;
    }

    @Override
    public RetentionConfiguration retentions() {
        return retentions;
    }

    @Override
    public void setRetentions( RetentionConfiguration strategy ) {
        this.retentions = strategy;
    }

    @Override
    public MetricCache metricCache() {
        return cache;
    }

    @Override
    public void setMetricCache( MetricCache cache ) {
        this.cache = cache;
    }
}
