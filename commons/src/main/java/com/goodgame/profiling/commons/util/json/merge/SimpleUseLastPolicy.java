package com.goodgame.profiling.commons.util.json.merge;

import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

/**
 * Make a shallow merge, i.e. copy over top-level values. If two values with the
 * same key exist, the last one is used.
 */
public class SimpleUseLastPolicy implements JSONMergePolicy {

    private static final Logger log = LogManager.getLogger();

    @Override
    public JSONObject merge( JSONObject first, JSONObject second ) {
        log.entry( first, second );

        JSONObject target = new JSONObject();
        @SuppressWarnings( "rawtypes" )
        Iterator keys;
        String key;

        keys = first.keys();
        while ( keys.hasNext() ) {
            key = (String) keys.next();
            target.put( key, first.get( key ) );
        }

        keys = second.keys();
        while ( keys.hasNext() ) {
            key = (String) keys.next();
            target.put( key, second.get( key ) );
        }

        return log.exit( target );
    }

}
