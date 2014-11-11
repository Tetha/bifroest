package com.goodgame.profiling.graphite_retentions.bootloader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.goodgame.profiling.commons.boot.interfaces.Subsystem;
import com.goodgame.profiling.commons.exception.ConfigurationException;
import com.goodgame.profiling.commons.statistics.units.SI_PREFIX;
import com.goodgame.profiling.commons.statistics.units.TIME_UNIT;
import com.goodgame.profiling.commons.statistics.units.parse.TimeUnitParser;
import com.goodgame.profiling.commons.statistics.units.parse.UnitParser;
import com.goodgame.profiling.commons.systems.SystemIdentifiers;
import com.goodgame.profiling.commons.systems.configuration.ConfigurationObserver;
import com.goodgame.profiling.commons.systems.configuration.EnvironmentWithJSONConfiguration;
import com.goodgame.profiling.commons.systems.configuration.InvalidConfigurationException;
import com.goodgame.profiling.graphite_retentions.RetentionConfiguration;
import com.goodgame.profiling.graphite_retentions.MutableRetentionConfiguration;
import com.goodgame.profiling.graphite_retentions.RetentionLevel;
import com.goodgame.profiling.graphite_retentions.RetentionTable;

import org.kohsuke.MetaInfServices;

@MetaInfServices
public class RetentionSystem< E extends EnvironmentWithJSONConfiguration & EnvironmentWithMutableRetentionStrategy > implements Subsystem<E> {

    private static final Logger log = LogManager.getLogger();

    private static final UnitParser parser = new TimeUnitParser( SI_PREFIX.ONE, TIME_UNIT.SECOND );

    @Override
    public String getSystemIdentifier() {
        return SystemIdentifiers.RETENTION;
    }

    @Override
    public Collection<String> getRequiredSystems() {
        return Arrays.asList();
    }

    @Override
    public void configure( final E environment ) throws ConfigurationException {
        try {
            environment.setRetentions( createRetentions( environment.getConfiguration() ) );
        } catch (InvalidConfigurationException e ) {
            throw new ConfigurationException(e);
        }
        environment.getConfigurationLoader().subscribe( new ConfigurationObserver() {

            @Override
            public void handleNewConfig( JSONObject config ) {
                try {
                    environment.setRetentions( createRetentions( config ) );
                } catch ( InvalidConfigurationException e ) {
                    log.error( e );
                }
            }

        } );
    }

    @Override
    public void boot( final E environment ) throws Exception {
        // nothing to do
    }

    @Override
    public void shutdown( E environment ) {
        // Nothing to shutdown
    }

    private static void checkTableNamePart( String partDesc, String tableNamePart ) throws InvalidConfigurationException {
        if ( tableNamePart.isEmpty() ) {
            throw new InvalidConfigurationException( partDesc + " " + tableNamePart + " cannot be empty" );
        }

        if ( tableNamePart.length() > 19 ) {
            throw new InvalidConfigurationException( partDesc + " " + tableNamePart + " cannot belonger than 8 characters" );
        }
        if ( !StringUtils.isAlphanumeric( tableNamePart ) ) {
            throw new InvalidConfigurationException( partDesc + " " + tableNamePart + " must be alphanumerical" );
        }
        if ( !tableNamePart.toLowerCase().equals( tableNamePart ) ) {
            throw new InvalidConfigurationException( partDesc + " " + tableNamePart + " must not contain capital letters" );
        }
        if ( tableNamePart.contains( RetentionTable.SEPARATOR_OF_MADNESS ) ) {
            throw new InvalidConfigurationException( partDesc + " " + tableNamePart + " cannot contain " + RetentionTable.SEPARATOR_OF_MADNESS );
        }
        if ( tableNamePart.contains( "X" ) ) {
            throw new InvalidConfigurationException( partDesc + " " + tableNamePart + " cannot contain X or it might form "
                    + RetentionTable.SEPARATOR_OF_MADNESS );
        }

        if ( tableNamePart.contains( "0" ) ) {
            throw new InvalidConfigurationException( partDesc + " " + tableNamePart + " cannot contain 0 or it might form "
                    + RetentionTable.SEPARATOR_OF_MADNESS );
        }
    }

    private static RetentionConfiguration createRetentions( JSONObject config ) throws InvalidConfigurationException {
        JSONObject retention = config.getJSONObject( "retention" );
        MutableRetentionConfiguration retentions = new MutableRetentionConfiguration();
        
        JSONObject levels = retention.getJSONObject("levels");
        
        JSONArray names = levels.names();
        
        if ( names == null || names.length() ==  0 ) {
        	throw new InvalidConfigurationException( "No retention levels specified" );
        }
        
        for ( int i = 0; i < names.length(); i++ ){
        	JSONObject level = levels.getJSONObject( names.getString( i ) );
        	String name = names.getString( i ).toLowerCase();
        	checkTableNamePart( "Retention Level Name", name );
        	long frequency = parser.parse( level.getString( "frequency" ) ).longValue();
        	long blockSize = parser.parse( level.getString( "blockSize" ) ).longValue();
        	long blocks = level.getLong( "blocks" ) + 1; //Add head
        	String next = level.optString( "next", null );
        	retentions.addLevel( new RetentionLevel( name, frequency, blocks, blockSize, next ) ) ;
        }
        
        retentions.getTopologicalSort();
        

        JSONArray patterns = retention.getJSONArray( "patterns" );
        for ( int i = 0; i < patterns.length(); i++ ) {
            JSONObject pattern = patterns.getJSONObject( i );
            String regex = pattern.getString( "pattern" );
            if ( pattern.has( "method" ) ) {
                retentions.addFunctionEntry( regex, pattern.getString( "method" ) );
            }
            if ( pattern. has( "readLevels" ) ) {
            	JSONArray readLevelsArray = pattern.getJSONArray("readLevels");
            	List<String> readLevelsList = new ArrayList<String>();
            	for(int j = 0; j < readLevelsArray.length(); j++){
            		readLevelsList.add(readLevelsArray.getString(j));
            	}
            	retentions.addReadLevelsEntry(regex, readLevelsList );
            }
            if ( pattern.has( "writeLevel" ) ) {
            	retentions.addWriteLevelEntry(regex, pattern.getString( "writeLevel" ) );
            }
            if ( pattern.has( "nameRetentions" ) ){
            	retentions.addNameRetentionEntry(regex, parser.parse(pattern.getString( "nameRetentions" ) ).longValue() );
            }
        }

        return retentions;
    }

}
