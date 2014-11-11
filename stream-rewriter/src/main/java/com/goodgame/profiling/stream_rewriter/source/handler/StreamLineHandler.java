package com.goodgame.profiling.stream_rewriter.source.handler;

import java.io.IOException;
import java.time.Clock;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.goodgame.profiling.commons.model.Metric;
import com.goodgame.profiling.rewrite_framework.core.drain.Drain;
import com.goodgame.profiling.rewrite_framework.core.source.handler.SourceUnitHandler;

public class StreamLineHandler implements SourceUnitHandler<String> {
    private static final Logger log = LogManager.getLogger();

    private final Drain drain;

    public StreamLineHandler( Drain drain ) {
        this.drain = drain;
    }

    @Override
    public void handleUnit( String line ) {
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
}
