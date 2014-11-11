package com.goodgame.profiling.rewrite_framework.core.source.timestamp;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

public class TimestampCreator {

    private static final Logger log = LogManager.getLogger();

    public static final long DEFAULT_WARN_THRESHOLD = 15 * 60;

    private static Map<String, Integer> PRIORITIES;

    public static int getTimestampPriority( String handledType ) {
        if ( PRIORITIES == null ) {
            PRIORITIES = new HashMap<String, Integer>();
            PRIORITIES.put( "default", 0 );
            PRIORITIES.put( "bifroest", 1 );
            PRIORITIES.put( "limits", 2 );
            PRIORITIES.put( "overrides", 3 );
        }
        return PRIORITIES.get( handledType );
    }

    private static final ServiceLoader<TimestampFactory> factories = ServiceLoader.load( TimestampFactory.class );

    public Timestamp loadConfiguration( JSONObject config ) {
        Timestamp timestamp = new DefaultTimestamp();

        if ( !config.has( "fetchtimes" ) ) {
            return log.exit( timestamp );
        }

        JSONObject tsConfig = config.getJSONObject( "fetchtimes" );
        for ( TimestampFactory factory : factories ) {
            if ( tsConfig.has( factory.handledType() ) ) {
                timestamp = factory.create( timestamp, tsConfig );
            }
        }
        return log.exit( timestamp );
    }

}
