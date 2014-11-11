package com.goodgame.profiling.commons.systems.statistics.push_strategy.plaintext_carbon;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.goodgame.profiling.commons.model.Metric;
import com.goodgame.profiling.commons.systems.cron.EnvironmentWithTaskRunner;
import com.goodgame.profiling.commons.systems.statistics.push_strategy.with_task.StatisticsPushStrategyWithTask;

public class PlainTextCarbonPushStrategy<E extends EnvironmentWithTaskRunner> extends StatisticsPushStrategyWithTask<E> {
    private static final Logger log = LogManager.getLogger();
    
    private final String carbonHost;
    private final int carbonPort;

    public PlainTextCarbonPushStrategy( E environment, String carbonHost, int carbonPort ) {
        super( environment );
        this.carbonHost = carbonHost;
        this.carbonPort = carbonPort;
    }

    @Override
    public void pushAll( Collection<Metric> metrics ) throws IOException {
        log.debug( "Number of metrics: {}", metrics.size() );
        StringBuilder buffer = new StringBuilder();
        for( Metric metric : metrics ) {
            log.trace( metric );
            String line = metric.name() + " " + metric.value() + " " + metric.timestamp() + "\n";
            buffer.append( line );
        }

        log.debug( "Opening Socket to {}:{}", carbonHost, carbonPort );
        try( Socket carbonSocket = new Socket( carbonHost, carbonPort )) {
            BufferedWriter toCarbon = new BufferedWriter( new OutputStreamWriter( carbonSocket.getOutputStream() ) );
            toCarbon.write( buffer.toString() );
            toCarbon.flush();
        }
        log.debug( "Closed Socket to {}:{}", carbonHost, carbonPort );
    }

    @Override
    public void closeAfterTaskStopped() throws IOException {
        // Don't do anything here. We cannot keep the connection open, because carbon doesn't support that.
    }
}
