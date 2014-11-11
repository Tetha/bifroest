package com.goodgame.profiling.graphite_bifroest.systems.rebuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.goodgame.profiling.commons.boot.interfaces.Subsystem;
import com.goodgame.profiling.commons.systems.SystemIdentifiers;
import com.goodgame.profiling.commons.systems.configuration.EnvironmentWithJSONConfiguration;
import com.goodgame.profiling.commons.systems.cron.EnvironmentWithTaskRunner;
import com.goodgame.profiling.commons.systems.cron.TaskRunner;
import com.goodgame.profiling.commons.util.json.JSONUtils;
import com.goodgame.profiling.graphite_bifroest.systems.BifroestIdentifiers;
import com.goodgame.profiling.graphite_bifroest.systems.cassandra.EnvironmentWithCassandra;
import com.goodgame.profiling.graphite_bifroest.systems.prefixtree.EnvironmentWithMutablePrefixTree;
import com.goodgame.profiling.graphite_bifroest.systems.prefixtree.PrefixTree;

public class TreeRebuilderSystem< E extends EnvironmentWithJSONConfiguration & EnvironmentWithTaskRunner & EnvironmentWithCassandra & EnvironmentWithMutablePrefixTree & EnvironmentWithMutableTreeRebuilder >
        implements Subsystem<E> {

    private static final Logger log = LogManager.getLogger();

    private static final long DEFAULT_RECOMPUTATION_DELAY = 600;

    private static final String DEFAULT_TREE_STORAGE = "/tmp/graphite/bifroest/tree";

    private Writer toStorageFile;

    @Override
    public String getSystemIdentifier() {
        return BifroestIdentifiers.REBUILDER;
    }

    @Override
    public Collection<String> getRequiredSystems() {
        return Arrays.asList( SystemIdentifiers.LOGGING, SystemIdentifiers.CONFIGURATION, SystemIdentifiers.STATISTICS, SystemIdentifiers.CRON,
                BifroestIdentifiers.CASSANDRA, SystemIdentifiers.RETENTION );
    }

    @Override
    public void boot( final E environment ) throws Exception {
        JSONObject config = JSONUtils.getWithDefault( environment.getConfiguration(), "bifroest", new JSONObject() );
        String storage = JSONUtils.getWithDefault( config, "treestorage", DEFAULT_TREE_STORAGE );

        boolean treeLoaded = false;
        try (BufferedReader fromStorageFile = new BufferedReader( new InputStreamReader( new GZIPInputStream( Files.newInputStream( Paths.get( storage ) ) ), Charset.forName("UTF-8") ) )) {
            PrefixTree tree = new PrefixTree();
            String line;
            int malformedLines = 0;
            int goodLines = 0;
            while( ( line = fromStorageFile.readLine() ) != null ) {
                String[] lineparts = StringUtils.split( line, ',' );
                if ( lineparts.length >= 2 ) {
                    goodLines++;
                    tree.addEntry( lineparts[0], Long.parseLong(lineparts[1]) );
                } else {
                    malformedLines++;
                    log.warn( "Ignored Line: " + line );
                }
            }
            log.info( "Parsed {} lines, Ignored {} lines in tree file", goodLines, malformedLines );
            environment.setTree( tree );
            treeLoaded = true;
        } catch ( IOException e ) {
            log.warn( "Exception while reading prefix tree from disk", e );
            environment.setTree( new PrefixTree() );
        }

        // Open this HERE, so this crashes early if we cannot write to this file.
        // Also, it is already open in case of an OOM, so we have a chance to successfully write the tree
        toStorageFile = new OutputStreamWriter( new GZIPOutputStream( Files.newOutputStream( Paths.get( storage ) ) ), Charset.forName("UTF-8") );


        TaskRunner cron = environment.taskRunner();

        long frequency = JSONUtils.getWithDefault( config, "recomputation-delay-in-seconds", DEFAULT_RECOMPUTATION_DELAY );
        long delay = 0;

        // Check if the tree system has found a file
        if ( !treeLoaded ) {
            log.warn( "Building tree from scratch - this could take some time!" );
            cron.runOnce( new Recomputation<E>( environment), "Recomputation", Duration.ZERO );
            delay = frequency;
        }

        environment.setRebuilder( new Rebuilder( environment) );
        cron.runRepeated( new Recomputation<E>( environment), "Recomputation", Duration.ofSeconds( delay ), Duration.ofSeconds( frequency ), true );
    }

    @Override
    public void shutdown( E environment ) {
        try {
            environment.getTree().forAllLeaves( ( metric, age ) -> {
                try {
                    toStorageFile.write( metric + "," + age + "\n" );
                } catch (Exception e) {
                    log.warn( "Exception while writing prefix tree to disk", e );
                }
            } );

            toStorageFile.close();
        } catch ( IOException e ) {
            log.warn( "Exception while writing prefix tree to disk", e );
        }
    }

    private class Rebuilder implements TreeRebuilder {

        private final E environment;

        public Rebuilder( E environment ) {
            this.environment = environment;
        }

        @Override
        public void rebuild() {
            TaskRunner cron = environment.taskRunner();
            cron.runOnce( new Recomputation<E>( environment ), "Recomputation", Duration.ZERO );
        }
    }
}
