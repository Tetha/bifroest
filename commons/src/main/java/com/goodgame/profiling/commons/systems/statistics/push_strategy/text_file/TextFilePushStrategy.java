package com.goodgame.profiling.commons.systems.statistics.push_strategy.text_file;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.goodgame.profiling.commons.model.Metric;
import com.goodgame.profiling.commons.output.AtomicWriteFileOutputStream;
import com.goodgame.profiling.commons.systems.cron.EnvironmentWithTaskRunner;
import com.goodgame.profiling.commons.systems.statistics.push_strategy.with_task.StatisticsPushStrategyWithTask;

public class TextFilePushStrategy<E extends EnvironmentWithTaskRunner> extends StatisticsPushStrategyWithTask<E> {
    private static final Logger log = LogManager.getLogger();

    private final Path path;

    public TextFilePushStrategy( E environment, Path path ) {
        super( environment );
        this.path = path;
    }

    @Override
    public void pushAll( Collection<Metric> metrics ) throws IOException {
        try( BufferedWriter writer = new BufferedWriter( new OutputStreamWriter( new AtomicWriteFileOutputStream( path ) ) )) {
            log.debug( "Writing metrics to {}", path );
            for( Metric metric : metrics ) {
                writer.write( metric.name() );
                writer.write( " " );
                writer.write( String.valueOf( metric.value() ) );
                writer.write( " " );
                writer.write( String.valueOf( metric.timestamp() ) );
                writer.newLine();
            }
        }
    }

    @Override
    public void closeAfterTaskStopped() throws IOException {
        // don't do anything
    }
}
