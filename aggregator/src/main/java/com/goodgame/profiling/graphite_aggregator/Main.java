package com.goodgame.profiling.graphite_aggregator;

import java.nio.file.Paths;

import com.goodgame.profiling.commons.boot.BootLoader;
import com.goodgame.profiling.commons.systems.configuration.JSONConfigurationSystem;
import com.goodgame.profiling.commons.systems.cron.CronSystem;
import com.goodgame.profiling.commons.systems.logging.Log4j2System;
import com.goodgame.profiling.commons.systems.net.multiserver.MultiServerSystem;
import com.goodgame.profiling.commons.systems.rmi_jmx.JMXSystem;
import com.goodgame.profiling.commons.systems.statistics.StatisticsSystem;
import com.goodgame.profiling.graphite_aggregator.systems.AggregatorEnvironment;
import com.goodgame.profiling.graphite_aggregator.systems.aggregation.AggregationSystem;
import com.goodgame.profiling.graphite_aggregator.systems.cassandra.CassandraSystem;
import com.goodgame.profiling.graphite_retentions.bootloader.RetentionSystem;

public class Main {

    public static void main( String[] args ) throws Exception {
        BootLoader<AggregatorEnvironment> loader = new BootLoader<>();
        AggregatorEnvironment environment = new AggregatorEnvironment( Paths.get( args[0] ), loader );

        loader.addSubsystem( new JSONConfigurationSystem<AggregatorEnvironment>() );
        loader.addSubsystem( new Log4j2System<AggregatorEnvironment>() );
        loader.addSubsystem( new StatisticsSystem<AggregatorEnvironment>() );
        loader.addSubsystem( new JMXSystem<AggregatorEnvironment>() );
        loader.addSubsystem( new CronSystem<AggregatorEnvironment>() );
        loader.addSubsystem( new MultiServerSystem<AggregatorEnvironment>() );
        loader.addSubsystem( new RetentionSystem<AggregatorEnvironment>() );
        loader.addSubsystem( new CassandraSystem<AggregatorEnvironment>() );
        loader.addSubsystem( new AggregationSystem<AggregatorEnvironment>() );

        loader.boot( environment );
    }
}
