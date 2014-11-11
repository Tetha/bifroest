package com.goodgame.profiling.commons.statistics.storage;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermissions;

import org.json.JSONObject;

public class JSONMetricStorageInFile extends JSONMetricStorage {

    private final Path fileOnDisk;

    public JSONMetricStorageInFile( Path fileOnDisk ) {
        super();
        this.fileOnDisk = fileOnDisk;
    }

    public JSONMetricStorageInFile( Path fileOnDisk, JSONObject storage ) {
        super( storage );
        this.fileOnDisk = fileOnDisk;
    }

    @Override
    public MetricStorage getSubStorageCalled( String subStorageName ) {
        JSONObject subStorage = storage().has( subStorageName ) ? storage().getJSONObject( subStorageName ) : new JSONObject();
        storage().put( subStorageName, subStorage );
        // deliberate object escape of sub-storage. This way we just need to
        // tell the top-level metric storage to dump the JSON into a file and we
        // will get the all sub storages, because the sub storages modify
        // different sub-objects of the JSON Object we have here.
        return new JSONMetricStorageInFile( fileOnDisk, subStorage );
    }

    @Override
    public void finishStoringTheMetrics() throws IOException {
        // We need to be careful with replacing the existing file, since we have
        // no information if and how a client is reading this file. Basically,
        // someone might be reading this file at every point in time and
        // whenever we try to touch it. Merely truncating the file or appending
        // to the file might result in funny but unproductive results. Thus we
        // write to a temporary file first and atomically replace the old file
        // with the new file.
        Path tempFile = Files.createTempFile( "gatherer_performance_file", ".json",
                PosixFilePermissions.asFileAttribute( PosixFilePermissions.fromString( "rw-r--r--" ) ) );
        try ( BufferedWriter tempFileWriter = Files.newBufferedWriter( tempFile, Charset.forName( "UTF-8" ), StandardOpenOption.WRITE ) ) {
            storage().write( tempFileWriter );
        }

        Files.move( tempFile, fileOnDisk, StandardCopyOption.REPLACE_EXISTING );
    }

}
