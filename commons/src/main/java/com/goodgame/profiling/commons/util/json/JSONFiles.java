package com.goodgame.profiling.commons.util.json;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Utility class containing some functions for reading, writing JSON to files.
 */
public class JSONFiles {

    private static final Logger log = LogManager.getLogger();

    public JSONFiles() {
        // Utility class - avoid instantiation
    }

    /**
     * Simply read JSON from a file.
     * 
     * If the file name is empty or null, or if the file does not exist or does
     * not contain valid JSON, an empty <code>JSONObject</code> is returned.
     */
    public static JSONObject readJSON( String file ) throws IOException {
        if ( file == null || file.isEmpty() ) {
            log.debug( "No output file given - starting new" );
            return new JSONObject();
        } else {
            try ( Reader r = new IgnoreCommentsReader( '#', Files.newBufferedReader( Paths.get( file ), Charset.forName( "UTF-8" ) ) ) ) {
                log.debug( "Existing file '" + file + "' found, attempting to parse contents" );
                JSONObject json = new JSONObject( new JSONTokener( r ) );
                return json;
            } catch ( JSONException e ) {
                log.debug( "File contains no valid JSON - overwriting file" );
                return new JSONObject();
            } catch ( NoSuchFileException e ) {
                log.debug( "Output file '" + file + "' does not exist yet - starting new" );
                return new JSONObject();
            }
        }
    }

    /**
     * Simply read JSON from a file. Ignores all lines starting with the given
     * comment character.
     * 
     * If the file name is empty or null, or if the file does not exist or does
     * not contain valid JSON, an empty <code>JSONObject</code> is returned.
     */
    public static JSONObject readJSONWithComments( String file, char commentsChar ) throws IOException {
        if ( file == null || file.isEmpty() ) {
            log.debug( "No output file given - starting new" );
            return new JSONObject();
        } else {
            try ( Reader r = new IgnoreCommentsReader( '#', Files.newBufferedReader( Paths.get( file ), Charset.forName( "UTF-8" ) ) ) ) {
                log.debug( "Existing file '" + file + "' found, attempting to parse contents" );
                JSONObject json = new JSONObject( new JSONTokener( r ) );
                return json;
            } catch ( JSONException e ) {
                log.debug( "File contains no valid JSON - overwriting file" );
                return new JSONObject();
            } catch ( NoSuchFileException e ) {
                log.debug( "Output file '" + file + "' does not exist yet - starting new" );
                return new JSONObject();
            }
        }
    }

    /**
     * Simply write JSON to a file.
     * 
     * Will create all necessary directories for the output file. If the file is
     * empty or null, the JSON is written to STDOUT instead.
     */
    public static void writeJSON( String file, JSONObject json ) throws IOException {
        Writer writer;
        if ( file == null || file.isEmpty() ) {
            log.debug( "No outputfile given - using STDOUT" );
            writer = new OutputStreamWriter( System.out );
        } else {
            log.debug( "Selected output file" + file );
            Path path = Paths.get( file );
            Path absolute = path.toAbsolutePath();
            if ( absolute.getNameCount() > 1 ) {
                absolute = absolute.subpath( 0, absolute.getNameCount() - 1 );
                Files.createDirectories( absolute );
            }
            writer = Files.newBufferedWriter( path, Charset.forName( "UTF-8" ) );
        }
        try {
            log.debug( "Writing JSON to file" );
            json.write(writer);
            writer.flush();
        } catch ( JSONException e ) {
            log.error( "Attempted to write invalid JSON", e );
            throw new IOException( "Attempted to write invalid JSON", e );
        } finally {
            writer.close();
        }
    }

}
