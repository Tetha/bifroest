package com.goodgame.profiling.rewrite_framework.systems.gatherer.optimization;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections4.map.LazyMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.goodgame.profiling.commons.statistics.aggregation.TotalAverageAggregation;
import com.goodgame.profiling.commons.systems.configuration.EnvironmentWithJSONConfiguration;

public class ThreadCountOptimizer {
    private static final Logger log = LogManager.getLogger();

    private static final int MAXCHANGE = 1;

    // How often should we try each new poolsize setting?
    private final int repeatOptimization;
    // How many try-runs have we done (for all new poolsize settings together)?
    private int optimizationRunCount;
    private int currentOptimalPoolSize;
    // Map from proposed new pool sizes to average runtimes
    private Map<Integer, TotalAverageAggregation> averageRuntimes;
    private boolean currentlyOptimizing;
    private final ShouldRunOptimizerStrategy strategy;
    private final Path persistenceFile;

    public static <E extends EnvironmentWithJSONConfiguration> ThreadCountOptimizer withDefaultStrategies( E env ) {
        return new ThreadCountOptimizer(
                env.getConfiguration().getJSONObject("threadcount-optimizer"),
                new AndStrategy(
                        new OfficeHoursStrategy(),
                        new DisabledByConfigStrategy<>(env)
                )
        );
    }

    public ThreadCountOptimizer( JSONObject config, ShouldRunOptimizerStrategy strategy ) {
        this.persistenceFile = Paths.get( config.getString( "persistenceFile" ) );
        try ( Reader r = Files.newBufferedReader( this.persistenceFile, Charset.forName( "UTF-8" ) ) ){
            JSONObject contents = new JSONObject( new JSONTokener( r ) );
            this.currentOptimalPoolSize = contents.getInt( "poolsize" );
            log.info( "Successfully read initial number of threads from state file ({})", this.currentOptimalPoolSize );
        } catch (JSONException | IOException e) {
            log.warn( "Couldn't read saved value for poolsize, using initialPoolSize from config!" );
            this.currentOptimalPoolSize = config.getInt( "initialPoolSize" );
        }

        this.repeatOptimization = config.getInt( "repeat" );
        this.optimizationRunCount = 0;
        this.averageRuntimes = LazyMap.<Integer, TotalAverageAggregation>lazyMap(
                new HashMap<>(),
                TotalAverageAggregation::new);
        this.strategy = strategy;
        this.currentlyOptimizing = false;
    }

    private int currentRunPoolSize() {
        if ( currentlyOptimizing ) {
            int currentRunCount = currentOptimalPoolSize + (optimizationRunCount % (2*MAXCHANGE+1)) - MAXCHANGE;
            return currentRunCount <= 0 ? 1 : currentRunCount;
        } else {
            return currentOptimalPoolSize;
        }
    }

    public int nextTreadCount() {
        if ( strategy.shouldRun( ) ) {
            currentlyOptimizing = true;
            optimizationRunCount++;
            log.info( "Run " + optimizationRunCount + " with " + currentRunPoolSize() + " threads.");
            return currentRunPoolSize() ;
        } else {
            currentlyOptimizing = false;
            optimizationRunCount = 0;
            log.info( "Not optimizing thread count!");
            return currentOptimalPoolSize ;
        }
    }

    public void recordRuntime( long runTimeInMillis ) {
        if ( currentlyOptimizing ) {
            averageRuntimes.get( currentRunPoolSize() ).consumeValue( runTimeInMillis );
        }

        if ( optimizationRunCount == (2*MAXCHANGE+1) * repeatOptimization ) {
            assert (!averageRuntimes.isEmpty());

            Optional<Integer> newOptimalPoolSize = averageRuntimes
                    .entrySet()
                    .stream()
                    .min( (a,b) -> Double.compare( a.getValue().getAggregatedValue(), b.getValue().getAggregatedValue() ) )
                    .map( entry -> entry.getKey() );

            if ( newOptimalPoolSize.get() == currentOptimalPoolSize ) {
                log.info( "Didn't change optimal pool size (" + currentOptimalPoolSize + ")" );
            } else {
                log.info( "New optimal pool size is: " + newOptimalPoolSize.get() );
                currentOptimalPoolSize = newOptimalPoolSize.get();
                saveToFile();
            }
            optimizationRunCount = 0;
            averageRuntimes.clear();
        }
    }

    private void saveToFile() {
        try ( Writer w = Files.newBufferedWriter( this.persistenceFile, Charset.forName( "UTF-8" ) ) ) {
            JSONObject contents = new JSONObject();
            contents.put( "poolsize", this.currentOptimalPoolSize );
            w.write( contents.toString() );
        } catch (IOException e) {
            log.warn( "Could not write to file {}", this.persistenceFile, e );
        }
    }

    // Package private for testing
    int currentOptimalPoolSize() {
        return currentOptimalPoolSize;
    }
}
