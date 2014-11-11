package com.goodgame.profiling.rewrite_framework.source.timestamp.override;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.kohsuke.MetaInfServices;

import com.goodgame.profiling.rewrite_framework.core.source.timestamp.Timestamp;
import com.goodgame.profiling.rewrite_framework.core.source.timestamp.TimestampCreator;
import com.goodgame.profiling.rewrite_framework.core.source.timestamp.TimestampFactory;

@MetaInfServices
public class OverrideTimestampFactory implements TimestampFactory {

    private static final Logger log = LogManager.getLogger();

    private Timestamp timestamp;
    private long warnThreshold;

    @Override
    public String handledType() {
        return "overrides";
    }

    @Override
    public Timestamp create( Timestamp oldTimestamp, JSONObject config ) {
        if ( !config.has( handledType() ) ) {
            return oldTimestamp;
        }
        warnThreshold = config.optLong( "warn-threshold", TimestampCreator.DEFAULT_WARN_THRESHOLD );

        timestamp = oldTimestamp;
        int prio = TimestampCreator.getTimestampPriority( handledType() );
        handleTimestamp( findTimestamp( timestamp, null, prio ), config );
        return log.exit( timestamp );
    }

    private OverrideTimestamp findTimestamp( Timestamp current, Timestamp previous, int priority ) {
        if ( current == null ) {
            return null;

        } else if ( priority > current.priority() ) {
            OverrideTimestamp newTimestamp = new OverrideTimestamp( current, priority, warnThreshold );
            if ( previous == null ) {
                timestamp = newTimestamp;
            } else {
                previous.append( newTimestamp );
            }
            return newTimestamp;

        } else if ( current instanceof OverrideTimestamp ) {
            return (OverrideTimestamp) current;

        } else {
            return findTimestamp( current.next(), current, priority );
        }
    }

    private void handleTimestamp( OverrideTimestamp timestamp, JSONObject config ) {
        if ( timestamp == null ) {
            return;
        }
        JSONObject fetchTimes = config.getJSONObject( handledType() );
        for ( String name : JSONObject.getNames( fetchTimes ) ) {
            try {
                timestamp.addOverride( name, fetchTimes.getLong( name ) );
            } catch ( JSONException e ) {
                String fetchTime = fetchTimes.getString( name );
                timestamp.addOverride( name, Long.parseLong( fetchTime ) );
            }
        }
    }

}
