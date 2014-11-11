package com.goodgame.profiling.rewrite_framework.source.timestamp.limit;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.goodgame.profiling.commons.statistics.jmx.MBeanManager;
import com.goodgame.profiling.commons.statistics.units.TIME_UNIT;
import com.goodgame.profiling.commons.statistics.units.format.TimeFormatter;
import com.goodgame.profiling.rewrite_framework.core.source.timestamp.Timestamp;

public class LimitTimestamp extends Timestamp implements LimitMBean {

    private static final Logger log = LogManager.getLogger();

    private final Map<Pattern, Long> limits;
    private final long warnThreshold;
    private int limitWasUsed = 0;

    public LimitTimestamp( Timestamp next, int priority, long warnThreshold ) {
        super( next, priority );
        this.limits = new HashMap<>();
        this.warnThreshold = warnThreshold;
        MBeanManager.registerStandardMBean( this, LimitMBean.class );
    }

    public void addLimit( String sourceId, long newTime ) {
        limits.put( Pattern.compile( sourceId ), newTime );
    }

    @Override
    public int getLimitWasUsed() {
        return limitWasUsed;
    }

    @Override
    public long getTime( long currentTime, String sourceId ) {
        log.entry( currentTime, sourceId );

        long currentDelta = Long.MAX_VALUE;

        for ( Entry<Pattern, Long> entry : limits.entrySet() ) {
            Matcher m = entry.getKey().matcher( sourceId );
            if ( m.find() && currentDelta > entry.getValue() ) {
                currentDelta = entry.getValue();
            }
        }

        long limitedTime = currentTime - currentDelta;
        long nextTime = next().getTime( currentTime, sourceId );

        if ( limitedTime > nextTime ) {
            limitWasUsed++;

            if ( currentDelta > warnThreshold ) {
            	TimeFormatter timeFormatter = new TimeFormatter( 1, TIME_UNIT.SECOND);
                log.warn( "The limit time for source " + sourceId + " is greater than " + timeFormatter.format(warnThreshold) + "!");
            }
            return log.exit( limitedTime );
        } else {
            return log.exit( nextTime );
        }
    }

}
