package com.goodgame.profiling.commons.systems.configuration;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.goodgame.profiling.commons.boot.interfaces.Subsystem;
import com.goodgame.profiling.commons.systems.SystemIdentifiers;
import com.goodgame.profiling.commons.systems.common.EnvironmentWithConfigPath;

@Deprecated
public class JSONConfigurationSystem< E extends EnvironmentWithConfigPath & EnvironmentWithMutableJSONConfiguration>
        implements Subsystem<E> {

    // bootloader v2 will make this work cleanly, too! --hkraemer
    private static final Logger log = LogManager.getLogger();

    @Override
    public String getSystemIdentifier() {
        return SystemIdentifiers.CONFIGURATION;
    }

    @Override
    public Collection<String> getRequiredSystems() {
        return Collections.emptyList();
    }

    @Override
    public void boot(E environment) {
        // TODO Allow configuration of loader at construction
        ActualJSONConfigurationLoader<E> loader = new ActualJSONConfigurationLoader<>(environment);
        environment.setConfigurationLoader(loader);
        try {
            loader.loadConfiguration();
        } catch (Exception e) {
            log.warn("Error in loadConfig", e);
        }

        if ( environment.getConfiguration() == null ) {
            throw new IllegalStateException( "Cannot load any config - exiting" );
        }

        registerOptionalStatusWriter( environment, loader );
    }

    private void registerOptionalStatusWriter( E environment, ActualJSONConfigurationLoader<E> loader ) {
        JSONObject configuration = environment.getConfiguration();
        if ( !configuration.has( "configuration" ) ) return;

        JSONObject configurationConfiguration = configuration.getJSONObject( "configuration" );
        if ( !configurationConfiguration.has( "status-file" ) ) return;

       ConfigurationObserver statusWriter = new ConfigurationLoadStatusWriter( configurationConfiguration.getString( "status-file" ), loader );
       
       // write the status of the initial config load
       statusWriter.handleNewConfig( configuration );

       loader.subscribe( statusWriter );
    }

    private static class ConfigurationLoadStatusWriter implements ConfigurationObserver {
        private final String destination;
        private final ActualJSONConfigurationLoader loader;

        public ConfigurationLoadStatusWriter( String destination, ActualJSONConfigurationLoader loader ) {
            this.destination = destination;
            this.loader = loader;
        }

        @Override
        public void handleNewConfig( JSONObject ignored ) {

            Path tmpFile = Paths.get( destination + "_new" );
            Path destinationFile = Paths.get( destination );

            log.debug( "Trying to write parse status to {} (tmp file used: {})", destinationFile, tmpFile );

            // We don't know if or when someoen reads the file, so atomic updates are necessary
            try {
                try ( BufferedWriter output = Files.newBufferedWriter( tmpFile, StandardOpenOption.CREATE_NEW ) ) {
                    computeJsonContent().write( output );
                }
                Files.move( Paths.get( destination + "_new" ), Paths.get( destination ), StandardCopyOption.REPLACE_EXISTING );
            } catch ( IOException e ) {
                log.warn( "Cannot write parse status temporarily to " + tmpFile + " and move it to " + destinationFile, e);
            }
        }

        private JSONObject computeJsonContent() {
            Map<Path, Optional<String>> newErrors = loader.getParseErrors();
            JSONObject statusFile = new JSONObject();
            JSONArray fileStati = new JSONArray();
            newErrors.entrySet()
                     .stream()
                     .map( e -> new JSONObject().put( "name", e.getKey().toString() )
                                                .put( "parse_error", e.getValue().orElse(null) ) )
                     .forEach( fileStati::put );
            statusFile.put( "files", fileStati );
            return statusFile;
        }
    }

    @Override
    public void shutdown(E environment) {
        // Nothing to shutdown
    }
}
