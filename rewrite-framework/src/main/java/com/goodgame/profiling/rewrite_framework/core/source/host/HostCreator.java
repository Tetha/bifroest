package com.goodgame.profiling.rewrite_framework.core.source.host;

import java.util.Collection;
import java.util.ServiceLoader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

public class HostCreator {
	private static final Logger log = LogManager.getLogger();

	private static final ServiceLoader<HostFactory> factories = ServiceLoader.load( HostFactory.class );

	public static Collection<String> create( JSONObject config ) {
		log.entry( config );
		for ( HostFactory factory : factories ) {
			if ( config.has( factory.handledType() ) ) {
				return log.exit( factory.create( config ) );
			}
		}
		throw new IllegalArgumentException( "Cannot handle host definition" );
	}
}
