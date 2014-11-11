package com.goodgame.profiling.commons.systems.configuration;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.goodgame.profiling.commons.systems.common.EnvironmentWithConfigPath;
import com.goodgame.profiling.commons.util.json.IgnoreCommentsReader;
import com.goodgame.profiling.commons.util.json.JSONUtils;

class ActualJSONConfigurationLoader< E extends EnvironmentWithConfigPath & EnvironmentWithMutableJSONConfiguration > implements JSONConfigurationLoader {

    private static final Logger log = LogManager.getLogger();

    private static final Pattern DEFAULT_FILEPATTERN = Pattern.compile( "\\.conf$" );
    private static final char DEFAULT_COMMENTCHAR = '#';

    private Pattern filePattern = DEFAULT_FILEPATTERN;
    private boolean ignoreComments = true;
    private char commentChar = DEFAULT_COMMENTCHAR;
    private boolean recurse = true;

    private final E environment;

    private final List<ConfigurationObserver> observers = new ArrayList<>();

    private final Map<Path, Optional<String>> parseErrors = new HashMap<Path, Optional<String>>();


    public ActualJSONConfigurationLoader(E environment) {
        this.environment = environment;
    }

    public Map<Path, Optional<String>> getParseErrors() {
        return parseErrors;
    }

    @Override
    public void setFilePattern( String pattern ) {
        this.filePattern = Pattern.compile( pattern );
    }

    @Override
    public void setIgnoreComments( boolean doIgnore ) {
        this.ignoreComments = doIgnore;
    }

    @Override
    public void setIgnoreComments( char ignoredChar ) {
        this.ignoreComments = true;
        this.commentChar = ignoredChar;
    }

    @Override
    public void setRecursive( boolean doRecurse ) {
        this.recurse = doRecurse;
    }

    private int maxDepth() {
         return recurse ? Integer.MAX_VALUE : 0;
    }

    @Override
    public void loadConfiguration() {
        parseErrors.clear();
        JSONFileVisitor visitor = new JSONFileVisitor();
        try {
            Files.walkFileTree( environment.getConfigPath(), EnumSet.of( FileVisitOption.FOLLOW_LINKS ), maxDepth(), visitor );
            environment.setConfiguration( visitor.configuration );

            for ( ConfigurationObserver observer : observers ) {
                try {
                    observer.handleNewConfig( visitor.configuration );
                } catch ( Exception e ) {
                    log.warn( "Something went wrong when notifying ConfigurationObserver:" + observer, e );
                }
            }
        } catch ( IOException e ) {
            log.error( "Could not load configuration", e );
            environment.setConfiguration( new JSONObject() );
        }
    }

    private class JSONFileVisitor extends SimpleFileVisitor<Path> {

        private JSONObject configuration = new JSONObject();

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            log.trace("Considering " + file);
            if (Files.isSymbolicLink(file)) {
                Path target = Files.readSymbolicLink(file);
                log.trace("following symlink " + file + " to " + target);
                Files.walkFileTree(target, EnumSet.of(FileVisitOption.FOLLOW_LINKS), maxDepth(), new JSONFileVisitor());
                return FileVisitResult.CONTINUE;
            }

            if ( !filePattern.matcher( file.getFileName().toString() ).find() ) {
                log.trace( "Skipping " + file );
                return FileVisitResult.CONTINUE;
            }

            log.debug("Loading from " + file);
            parseErrors.put(file, Optional.empty());
            Reader reader = null;
            try {
                reader = Files.newBufferedReader( file, Charset.forName( "UTF-8" ) );
                if ( ignoreComments ) {
                    reader = new IgnoreCommentsReader( commentChar, reader );
                }
                JSONObject contents = new JSONObject( new JSONTokener( reader ) );
                configuration = JSONUtils.deepMergeObjectsFailing( configuration, contents );
            } catch ( Exception e ) {
                log.warn( "Ignoring config file " + file, e );
                parseErrors.put(file, Optional.of(e.getMessage()));
            } finally {
                if ( reader != null ) {
                    reader.close();
                }
            }

            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult preVisitDirectory( Path dir, BasicFileAttributes attrs ) throws IOException {
            if ( dir.getFileName().startsWith( "." ) ) {
                log.trace( "Skipping directory " + dir );
                return FileVisitResult.SKIP_SUBTREE;
            } else {
                return FileVisitResult.CONTINUE;
            }
        }
    }

    @Override
    public void subscribe( ConfigurationObserver observer ) {
        observers.add( observer );
    }

    @Override
    public void unsubscribe( ConfigurationObserver observer ) {
        observers.remove( observer );
    }
}
