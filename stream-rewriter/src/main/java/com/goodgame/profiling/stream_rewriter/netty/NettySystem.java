package com.goodgame.profiling.stream_rewriter.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.Arrays;
import java.util.Collection;

import org.json.JSONObject;

import com.goodgame.profiling.commons.boot.interfaces.Subsystem;
import com.goodgame.profiling.commons.systems.SystemIdentifiers;
import com.goodgame.profiling.commons.systems.configuration.EnvironmentWithJSONConfiguration;
import com.goodgame.profiling.rewrite_framework.core.drain.Drain;
import com.goodgame.profiling.rewrite_framework.core.drain.DrainCreator;

public class NettySystem<E extends EnvironmentWithJSONConfiguration> implements Subsystem<E> {
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private Drain drain;

    @Override
    public String getSystemIdentifier() {
        return "NETTY";
    }

    @Override
    public Collection<String> getRequiredSystems() {
        return Arrays.asList( SystemIdentifiers.LOGGING, SystemIdentifiers.CONFIGURATION, SystemIdentifiers.STATISTICS);
    }

    @Override
    public void boot( E environment ) throws Exception {
        JSONObject subconfig = environment.getConfiguration().getJSONObject( "netty" );
        this.drain = new DrainCreator<>().loadConfiguration( environment, subconfig );

        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup( subconfig.getInt( "thread-count" ) );

        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
         .channel(NioServerSocketChannel.class)
         .childHandler(new NettyInitializer(drain));

        b.bind(subconfig.getInt( "port" ) ).sync();
    }

    @Override
    public void shutdown( E environment ) {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
