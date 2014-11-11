package com.goodgame.profiling.commons.util.panic;

import java.io.IOException;
import java.io.Writer;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

public class DumpAllThreads implements PanicAction {
    private static final Logger log = LogManager.getLogger();

    private static final Duration DELETE_TRACES_AFTER = Duration.ofDays( 30 );
    private static final Duration COOLDOWN = Duration.ofSeconds( 10 );

    private final Path folder;
    
    public static DumpAllThreads fromFullConfig( JSONObject fullconfig ) {
        JSONObject config = fullconfig.getJSONObject( "panic" );
        return new DumpAllThreads( Paths.get( config.getString( "threaddump-folder" ) ) );
    }

    public DumpAllThreads( Path folder ) {
        this.folder = folder;
    }

    @Override
    public void execute( Instant now ) {
        if ( !folder.toFile().exists() ) {
            boolean dirCreated = folder.toFile().mkdirs();
            if( !dirCreated ) {
                log.warn( "Could not create thread dump folder " + folder + "!" );
            }
        }

        String panicFileName = getCurrentPanicFileName( now );
        Path panicFile = folder.resolve( panicFileName );
        log.info( "Generating Threaddump in " + panicFile );
        try( Writer currentOutput = Files.newBufferedWriter( panicFile, Charset.forName( "UTF-8" ) )) {
            ThreadDumpFormatter formatter = getFormatter( currentOutput );
            ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
            formatter.formatThreadDump( mxBean.getThreadInfo( mxBean.getAllThreadIds() ), Thread.getAllStackTraces() );
        } catch( IOException e ) {
            log.warn( "Could not dump threads", e );
        }
    }

    /**
     * Deletes old thread dump folders that are older than DELETE_TRACES_AFTER
     */
    public void cleanOldFiles() {
        log.info( "Removing old threaddumps" );
        try ( Stream<Path> stream = Files.find( folder, 1, (path, attributes) -> true ) ) {
            stream.filter( path -> path.getFileName().toString().startsWith( "panic_" ) )
                  .filter( path ->
                      ZonedDateTime.from( DateTimeFormatter.ISO_LOCAL_DATE.parse( path.getFileName().toString().substring( "panic_".length() ) ) )
                      .plus( DELETE_TRACES_AFTER )
                      .isBefore( ZonedDateTime.ofInstant( Instant.now(), ZoneId.of( "Europe/Berlin" ) ) ) )
                  .forEach( path -> path.toFile().delete() );
        } catch ( IOException e ) {
            log.warn( "Exception while deleting old threaddumps", e );
        }
    }

    private static ThreadDumpFormatter getFormatter( Writer currentOutput ) {
        return new FullThreadDumpFormatter( currentOutput );
    }

    private static String getCurrentPanicFileName( Instant now ) {
        return "thread_dump_" + ZonedDateTime.ofInstant( now, ZoneId.of( "Europe/Berlin" ) ).format( DateTimeFormatter.ISO_LOCAL_DATE_TIME );
    }

    @Override
    public Duration getCooldown() {
        return COOLDOWN;
    }
}
