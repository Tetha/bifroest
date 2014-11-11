package com.goodgame.profiling.rewrite_framework.source.timestamp.override;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.goodgame.profiling.commons.statistics.units.TIME_UNIT;
import com.goodgame.profiling.commons.statistics.units.format.TimeFormatter;
import com.goodgame.profiling.rewrite_framework.core.source.timestamp.Timestamp;

public class OverrideTimestamp extends Timestamp {

    private static final Logger log = LogManager.getLogger();

    private final Map<String, Long> overrides;
    private final long warnThreshold;

    public OverrideTimestamp( Timestamp next, int priority, long warnThreshold ) {
        super( next, priority );
        this.overrides = new HashMap<String, Long>();
        this.warnThreshold = warnThreshold;
    }

    public void addOverride( String sourceId, long newTime ) {
        overrides.put( sourceId, newTime );
    }

    @Override
    public long getTime( long currentTime, String sourceId ) {
        log.entry( currentTime, sourceId );

        if ( overrides.containsKey( sourceId ) ) {
            long sourceStartTimestamp = overrides.get( sourceId );
            log.warn( "Attention: Overriding fetch time of source " + sourceId + " from " + currentTime + " to " + sourceStartTimestamp );
            if ( currentTime - warnThreshold > sourceStartTimestamp ) {
            	TimeFormatter timeFormatter = new TimeFormatter( 1, TIME_UNIT.SECOND);
                log.warn( "The fetch time of source " + sourceId + " is greater than " + timeFormatter.format(warnThreshold) + "!");
            }
            return log.exit( sourceStartTimestamp );

        } else {
            return log.exit( next().getTime( currentTime, sourceId ) );
        }
    }

}
