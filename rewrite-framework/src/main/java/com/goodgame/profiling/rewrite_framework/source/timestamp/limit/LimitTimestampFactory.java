package com.goodgame.profiling.rewrite_framework.source.timestamp.limit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.kohsuke.MetaInfServices;

import com.goodgame.profiling.commons.statistics.units.SI_PREFIX;
import com.goodgame.profiling.commons.statistics.units.TIME_UNIT;
import com.goodgame.profiling.commons.statistics.units.parse.TimeUnitParser;
import com.goodgame.profiling.commons.statistics.units.parse.UnitParser;
import com.goodgame.profiling.rewrite_framework.core.source.timestamp.Timestamp;
import com.goodgame.profiling.rewrite_framework.core.source.timestamp.TimestampCreator;
import com.goodgame.profiling.rewrite_framework.core.source.timestamp.TimestampFactory;

@MetaInfServices
public class LimitTimestampFactory implements TimestampFactory {

    private static final Logger log = LogManager.getLogger();

    private static final UnitParser parser = new TimeUnitParser( SI_PREFIX.ONE, TIME_UNIT.SECOND );

    private Timestamp timestamp;
    private long warnThreshold;

    @Override
    public String handledType() {
        return "limits";
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

    private LimitTimestamp findTimestamp( Timestamp current, Timestamp previous, int priority ) {
        if ( current == null ) {
            return null;

        } else if ( priority > current.priority() ) {
            LimitTimestamp newTimestamp = new LimitTimestamp( current, priority, warnThreshold );
            if ( previous == null ) {
                timestamp = newTimestamp;
            } else {
                previous.append( newTimestamp );

            }
            return newTimestamp;

        } else if ( current instanceof LimitTimestamp ) {
            return (LimitTimestamp) current;

        } else {
            return findTimestamp( current.next(), current, priority );
        }
    }

    private void handleTimestamp( LimitTimestamp timestamp, JSONObject config ) {
        if ( timestamp == null ) {
            return;
        }
        JSONObject fetchLimits = config.getJSONObject( handledType() );
        for ( String name : JSONObject.getNames( fetchLimits ) ) {
            try {
                timestamp.addLimit( name, fetchLimits.getLong( name ) );
            } catch ( JSONException e ) {
                String limit = fetchLimits.getString( name );
                timestamp.addLimit( name, parser.parse( limit ).longValue() );
            }
        }
    }

}
