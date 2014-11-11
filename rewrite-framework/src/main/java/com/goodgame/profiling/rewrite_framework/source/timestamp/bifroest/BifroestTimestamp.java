package com.goodgame.profiling.rewrite_framework.source.timestamp.bifroest;

import static com.goodgame.profiling.rewrite_framework.source.HostnameInverter.invertHostname;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.goodgame.profiling.commons.statistics.units.TIME_UNIT;
import com.goodgame.profiling.commons.statistics.units.format.TimeFormatter;
import com.goodgame.profiling.commons.util.json.JSONClient;
import com.goodgame.profiling.rewrite_framework.core.source.timestamp.Timestamp;

public final class BifroestTimestamp extends Timestamp {
    private static final Logger log = LogManager.getLogger();
    private final long warnThreshold;
    private final JSONClient bifroest;
    private final boolean invertHostname;

    public BifroestTimestamp( Timestamp next, int priority, long warnThreshold, int port, String host, boolean invertHostname ) {
        super( next, priority );
        this.warnThreshold = warnThreshold;
        this.bifroest = new JSONClient( host, port );
        this.invertHostname = invertHostname;
    }

    @Override
    public long getTime( long currentTime, String sourceId ) {
        log.entry( currentTime, sourceId );

        JSONObject command = new JSONObject();
        command.put( "command", "get-metric-age" );
        if ( invertHostname ) {
            command.put( "metric-prefix", "server." + invertHostname( sourceId ) );
        } else {
            command.put( "metric-prefix", "server." + sourceId );
        }
        JSONObject answer = null;
        try {
            answer = bifroest.request( command );
        } catch( IOException e ) {
            log.warn( "No answer from Bifroest!", e );
            return log.exit( next().getTime( currentTime, sourceId ) );
        }

        if ( answer.getBoolean( "found" ) ) {
            long age = answer.getLong( "age" );
            if ( age < currentTime - warnThreshold ) {
                TimeFormatter timeFormatter = new TimeFormatter( 1, TIME_UNIT.SECOND );
                log.warn( "The BifroestTimestamp of the latest data in " + sourceId + " is older than " + timeFormatter.format( warnThreshold ) + "!" );
            }
            return log.exit( age );
        } else {
            return log.exit( next().getTime( currentTime, sourceId ) );
        }
    }
}
