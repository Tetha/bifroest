package com.goodgame.profiling.stream_rewriter;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.goodgame.profiling.commons.boot.BootLoader;
import com.goodgame.profiling.commons.boot.InitD;
import com.goodgame.profiling.commons.systems.configuration.JSONConfigurationSystem;
import com.goodgame.profiling.commons.systems.cron.CronSystem;
import com.goodgame.profiling.commons.systems.logging.Log4j2System;
import com.goodgame.profiling.commons.systems.net.multiserver.MultiServerSystem;
import com.goodgame.profiling.commons.systems.rmi_jmx.JMXSystem;
import com.goodgame.profiling.commons.systems.statistics.StatisticsSystem;
import com.goodgame.profiling.commons.util.panic.DumpAllThreads;
import com.goodgame.profiling.commons.util.panic.ProfilingPanic;
import com.goodgame.profiling.graphite_retentions.bootloader.RetentionSystem;
import com.goodgame.profiling.rewrite_framework.systems.GathererEnvironment;
import com.goodgame.profiling.rewrite_framework.systems.persistent_drains.PersistentDrainSystem;
import com.goodgame.profiling.stream_rewriter.netty.NettySystem;

public class Main {
    public static class StreamRewriterEnvironment extends GathererEnvironment<String, String> {

        public StreamRewriterEnvironment(Path configPath, InitD init) {
            super(configPath, init);
        }
    }

    public static void main(String[] args) throws Exception {
        BootLoader<StreamRewriterEnvironment> loader = new BootLoader<>();
        StreamRewriterEnvironment environment = new StreamRewriterEnvironment(
                Paths.get(args[0]), loader);

        loader.addSubsystem(new JSONConfigurationSystem<StreamRewriterEnvironment>());
        loader.addSubsystem(new Log4j2System<StreamRewriterEnvironment>());
        loader.addSubsystem(new StatisticsSystem<StreamRewriterEnvironment>());
        loader.addSubsystem(new JMXSystem<StreamRewriterEnvironment>());
        loader.addSubsystem(new CronSystem<StreamRewriterEnvironment>());
        loader.addSubsystem(new MultiServerSystem<StreamRewriterEnvironment>());
        loader.addSubsystem(new PersistentDrainSystem<StreamRewriterEnvironment>());
        loader.addSubsystem(new RetentionSystem<StreamRewriterEnvironment>());
        loader.addSubsystem(new NettySystem<StreamRewriterEnvironment>());
        loader.boot(environment);

        ProfilingPanic.INSTANCE.addAction( DumpAllThreads.fromFullConfig( environment.getConfiguration() ) );
    }
}
