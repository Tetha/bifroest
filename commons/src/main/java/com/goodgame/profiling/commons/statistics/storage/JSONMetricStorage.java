package com.goodgame.profiling.commons.statistics.storage;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

public class JSONMetricStorage implements ReadableMetricStorage {

    private static final Logger log = LogManager.getLogger();

    private final JSONObject internalStorage;

    public JSONMetricStorage() {
        this( new JSONObject() );
    }

    public JSONMetricStorage( JSONObject storage ) {
        this.internalStorage = storage;
    }

    protected JSONObject storage() {
        return internalStorage;
    }

    @Override
    public JSONObject storageAsJSON() {
        return storage();
    }

    private void warnAboutOverwrites( String key, Object value ) {
        if ( internalStorage.has( key ) ) {
            log.warn( "Overriding key=" + key + " from " + internalStorage.get( key ).toString() + " to " + value );
        }
    }

    @Override
    public void store( String key, double value ) {
        int index = key.indexOf( '.' );
        if ( index >= 0 ) {
            getSubStorageCalled( key.substring( 0, index ) ).store( key.substring( index + 1 ), value );
        } else {
            warnAboutOverwrites( key, value );
            internalStorage.put( key, value );
        }
    }

    @Override
    public MetricStorage getSubStorageCalled( String subStorageName ) {
        JSONObject subStorage = this.internalStorage.has( subStorageName ) ? this.internalStorage.getJSONObject( subStorageName ) : new JSONObject();
        this.internalStorage.put( subStorageName, subStorage );
        // deliberate object escape of sub-storage. This way we just need to
        // tell the top-level metric storage to dump the JSON into a file and we
        // will get the all sub storages, because the sub storages modify
        // different sub-objects of the JSON Object we have here.
        return new JSONMetricStorage( subStorage );
    }

    @Override
    public void finishStoringTheMetrics() throws IOException {
        // Nothing to do here
    }

}
