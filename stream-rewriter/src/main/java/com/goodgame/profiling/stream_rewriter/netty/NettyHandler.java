package com.goodgame.profiling.stream_rewriter.netty;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.IOException;
import java.time.Clock;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.goodgame.profiling.commons.model.Metric;
import com.goodgame.profiling.rewrite_framework.core.drain.Drain;

@Sharable
public class NettyHandler extends SimpleChannelInboundHandler<String> {
    private static final Logger log = LogManager.getLogger();

    private final Drain drain;

    public NettyHandler( Drain drain ) {
        this.drain = drain;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String line) {
        String lineparts[] = StringUtils.split( line );

        if( lineparts.length != 2 && lineparts.length != 3 ) {
            throw new UnsupportedOperationException( "Cannot parse line " + line );
        }

        String metricName = lineparts[0];
        try {
            double value = Double.valueOf( lineparts[1] );
            long timestamp = lineparts.length == 3
                    ? Long.valueOf( lineparts[2] )
                    : Clock.systemUTC().instant().getEpochSecond();

            Metric metric = new Metric( metricName, timestamp, value );

            drain.output( Arrays.asList( metric ) );
        } catch ( NumberFormatException e ) {
            log.warn( "Cannot parse numbers in line " + line, e );
        } catch( IOException e ) {
            log.warn( "Exception while outputting metrics", e );
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.warn( "Exception in NettyHandler", cause );
        ctx.close();
    }
}
