package com.goodgame.profiling.rewrite_framework.core.source.handler;

import java.util.ServiceLoader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.goodgame.profiling.rewrite_framework.core.config.FetchConfiguration;
import com.goodgame.profiling.rewrite_framework.core.source.Source;

public class SourceHandlerCreator<I, U> {
	private static final Logger log = LogManager.getLogger();

	private final Class<I> inputType;
	private final Class<U> unitType;

	@SuppressWarnings("rawtypes")
	private static final ServiceLoader<SourceHandlerFactory> factories = ServiceLoader.load( SourceHandlerFactory.class );

	public SourceHandlerCreator( Class<I> inputType, Class<U> unitType ) {
		this.inputType = inputType;
		this.unitType = unitType;
	}

	@SuppressWarnings("unchecked")
	public SourceUnitHandler<U> create( Source<U> source, FetchConfiguration<I, U> fetchConfig ) {
		for ( SourceHandlerFactory<I, U> factory : factories ) {

			if ( !factory.handledInput().isAssignableFrom( inputType ) ) {
				continue;

			} else if ( !factory.handledUnit().isAssignableFrom( unitType ) ) {
				continue;

			} else {
				return log.exit( factory.create( source, fetchConfig ) );
			}
		}
		throw new IllegalArgumentException( "Cannot handle input type " + inputType.getName() + " and unit type " + unitType.getName() );
	}
}
