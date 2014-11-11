package com.goodgame.profiling.graphite_bifroest;

import java.nio.file.Paths;

import com.goodgame.profiling.commons.boot.BootLoader;
import com.goodgame.profiling.commons.systems.configuration.JSONConfigurationSystem;
import com.goodgame.profiling.commons.systems.cron.CronSystem;
import com.goodgame.profiling.commons.systems.logging.Log4j2System;
import com.goodgame.profiling.commons.systems.net.multiserver.MultiServerSystem;
import com.goodgame.profiling.commons.systems.rmi_jmx.JMXSystem;
import com.goodgame.profiling.commons.systems.statistics.StatisticsSystem;
import com.goodgame.profiling.graphite_bifroest.metric_cache.MetricCacheSystem;
import com.goodgame.profiling.graphite_bifroest.systems.BifroestEnvironment;
import com.goodgame.profiling.graphite_bifroest.systems.cassandra.CassandraSystem;
import com.goodgame.profiling.graphite_bifroest.systems.rebuilder.TreeRebuilderSystem;
import com.goodgame.profiling.graphite_retentions.bootloader.RetentionSystem;

public class Main {

    public static void main( String... args ) throws Exception {
        BootLoader<BifroestEnvironment> loader = new BootLoader<>();
        BifroestEnvironment environment = new BifroestEnvironment( Paths.get( args[0] ), loader );

        loader.addSubsystem( new JSONConfigurationSystem<BifroestEnvironment>() );
        loader.addSubsystem( new Log4j2System<BifroestEnvironment>() );
        loader.addSubsystem( new StatisticsSystem<BifroestEnvironment>() );
        loader.addSubsystem( new JMXSystem<BifroestEnvironment>() );
        loader.addSubsystem( new CronSystem<BifroestEnvironment>() );
        loader.addSubsystem( new MultiServerSystem<BifroestEnvironment>() );
        loader.addSubsystem( new CassandraSystem<BifroestEnvironment>() );
        loader.addSubsystem( new TreeRebuilderSystem<BifroestEnvironment>() );
        loader.addSubsystem( new RetentionSystem<BifroestEnvironment>() );
        loader.addSubsystem( new MetricCacheSystem<>() );

        loader.boot( environment );
    }

}
