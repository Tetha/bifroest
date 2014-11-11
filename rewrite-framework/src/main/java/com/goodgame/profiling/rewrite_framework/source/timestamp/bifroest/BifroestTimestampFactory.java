package com.goodgame.profiling.rewrite_framework.source.timestamp.bifroest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.kohsuke.MetaInfServices;

import com.goodgame.profiling.rewrite_framework.core.source.timestamp.Timestamp;
import com.goodgame.profiling.rewrite_framework.core.source.timestamp.TimestampCreator;
import com.goodgame.profiling.rewrite_framework.core.source.timestamp.TimestampFactory;

@MetaInfServices
public class BifroestTimestampFactory implements TimestampFactory {

    private static final Logger log = LogManager.getLogger();

    private Timestamp timestamp;
    private long warnThreshold;
    private int port;
    private String host;
    private boolean invertHostname;

    @Override
    public String handledType() {
        return "bifroest";
    }

    @Override
    public Timestamp create( Timestamp oldTimestamp, JSONObject config ) {
        if ( !config.has( handledType() ) ) {
            return oldTimestamp;
        }
        warnThreshold = config.optLong( "warn-threshold", TimestampCreator.DEFAULT_WARN_THRESHOLD );
        port = config.getJSONObject( handledType() ).getInt( "port" );
        host = config.getJSONObject( handledType() ).getString( "host" );
        invertHostname = config.getJSONObject( handledType() ).getBoolean( "invert-hostname" );

        timestamp = oldTimestamp;
        int prio = TimestampCreator.getTimestampPriority( handledType() );
        findTimestamp( timestamp, null, prio );
        return log.exit( timestamp );
    }

    private BifroestTimestamp findTimestamp( Timestamp current, Timestamp previous, int priority ) {
        if ( current == null ) {
            return null;
        } else {
            if ( priority > current.priority() ) {
                BifroestTimestamp newTimestamp = new BifroestTimestamp( current, priority, warnThreshold, port, host, invertHostname );
                if ( previous == null ) {
                    timestamp = newTimestamp;
                } else {
                    previous.append( newTimestamp );
                }
                return newTimestamp;
            } else {
                if ( current instanceof BifroestTimestamp ) {
                    return (BifroestTimestamp) current;

                } else {
                    return findTimestamp( current.next(), current, priority );
                }
            }
        }
    }
}
