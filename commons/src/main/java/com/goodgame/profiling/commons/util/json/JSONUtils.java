package com.goodgame.profiling.commons.util.json;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.goodgame.profiling.commons.statistics.units.SI_PREFIX;
import com.goodgame.profiling.commons.statistics.units.TIME_UNIT;
import com.goodgame.profiling.commons.statistics.units.parse.TimeUnitParser;
import com.goodgame.profiling.commons.util.json.merge.DeepMergeFailingPolicy;
import com.goodgame.profiling.commons.util.json.merge.DeepMergeUseFirstPolicy;
import com.goodgame.profiling.commons.util.json.merge.DeepMergeUseLastPolicy;
import com.goodgame.profiling.commons.util.json.merge.SimpleUseFirstPolicy;
import com.goodgame.profiling.commons.util.json.merge.SimpleUseLastPolicy;

/**
 * Utility class containing some functions to work with JSON objects.
 */
public class JSONUtils {

    public static JSONObject mergeObjects( JSONObject first, JSONObject second ) {
        return new SimpleUseFirstPolicy().merge( first, second );
    }

    public static JSONObject simpleMergeObjectsUseFirst( JSONObject first, JSONObject second ) {
        return new SimpleUseFirstPolicy().merge( first, second );
    }

    public static JSONObject simpleMergeObjectsUseLast( JSONObject first, JSONObject second ) {
        return new SimpleUseLastPolicy().merge( first, second );
    }

    public static JSONObject deepMergeObjectsUseFirst( JSONObject first, JSONObject second ) {
        return new DeepMergeUseFirstPolicy().merge( first, second );
    }

    public static JSONObject deepMergeObjectsUseLast( JSONObject first, JSONObject second ) {
        return new DeepMergeUseLastPolicy().merge( first, second );
    }

    public static JSONObject deepMergeObjectsFailing( JSONObject first, JSONObject second ) {
        return new DeepMergeFailingPolicy().merge( first, second );
    }

    public static JSONObject getWithDefault( JSONObject config, String key, JSONObject def ) {
        return config.has( key ) ? config.getJSONObject( key ) : def;
    }

    public static JSONArray getWithDefault( JSONObject config, String key, JSONArray def ) {
        return config.has( key ) ? config.getJSONArray( key ) : def;
    }

    public static String getWithDefault( JSONObject config, String key, String def ) {
        return config.has( key ) ? config.getString( key ) : def;
    }

    public static boolean getWithDefault( JSONObject config, String key, boolean def ) {
        return config.has( key ) ? config.getBoolean( key ) : def;
    }

    public static int getWithDefault( JSONObject config, String key, int def ) {
        return config.has( key ) ? config.getInt( key ) : def;
    }

    public static long getWithDefault( JSONObject config, String key, long def ) {
        return config.has( key ) ? config.getLong( key ) : def;
    }

    public static double getWithDefault( JSONObject config, String key, double def ) {
        return config.has( key ) ? config.getDouble( key ) : def;
    }

    public static Object[] getArray( String key, JSONObject object ) {
        if ( !object.has( key ) ) {
            return new Object[0];
        }
        JSONArray array = object.getJSONArray( key );
        Object[] result = new Object[array.length()];
        for ( int i = 0; i < array.length(); i++ ) {
            result[i] = array.get( i );
        }
        return result;
    }

    public static String[] getStringArray( String key, JSONObject object ) {
        if ( !object.has( key ) ) {
            return new String[0];
        }
        JSONArray array = object.getJSONArray( key );
        String[] result = new String[array.length()];
        for ( int i = 0; i < array.length(); i++ ) {
            result[i] = array.getString( i );
        }
        return result;
    }

    public static boolean[] getBooleanArray( String key, JSONObject object ) {
        if ( !object.has( key ) ) {
            return new boolean[0];
        }
        JSONArray array = object.getJSONArray( key );
        boolean[] result = new boolean[array.length()];
        for ( int i = 0; i < array.length(); i++ ) {
            result[i] = array.getBoolean( i );
        }
        return result;
    }

    public static int[] getIntArray( String key, JSONObject object ) {
        if ( !object.has( key ) ) {
            return new int[0];
        }
        JSONArray array = object.getJSONArray( key );
        int[] result = new int[array.length()];
        for ( int i = 0; i < array.length(); i++ ) {
            result[i] = array.getInt( i );
        }
        return result;
    }

    public static long[] getLongArray( String key, JSONObject object ) {
        if ( !object.has( key ) ) {
            return new long[0];
        }
        JSONArray array = object.getJSONArray( key );
        long[] result = new long[array.length()];
        for ( int i = 0; i < array.length(); i++ ) {
            result[i] = array.getLong( i );
        }
        return result;
    }

    public static double[] getDoubleArray( String key, JSONObject object ) {
        if ( !object.has( key ) ) {
            return new double[0];
        }
        JSONArray array = object.getJSONArray( key );
        double[] result = new double[array.length()];
        for ( int i = 0; i < array.length(); i++ ) {
            result[i] = array.getDouble( i );
        }
        return result;
    }

    public static Iterable<String> keys( JSONObject object ) {
        List<String> result = new ArrayList<>();
        for ( Object key : object.keySet() ) {
            result.add( (String) key );
        }
        return result;
    }

    public static long getTime( String key, JSONObject object, TIME_UNIT unit ) {
        try {
            long seconds = object.getLong( key );
            return seconds * (long) ( TIME_UNIT.SECOND.getMultiplier() / unit.getMultiplier() );
        } catch ( JSONException e ) {
            TimeUnitParser parser = new TimeUnitParser( SI_PREFIX.ONE, unit );
            return parser.parse( object.getString( key ) ).longValue();
        }
    }

}
