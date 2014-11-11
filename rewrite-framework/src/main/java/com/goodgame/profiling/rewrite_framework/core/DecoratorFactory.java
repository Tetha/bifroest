package com.goodgame.profiling.rewrite_framework.core;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.goodgame.profiling.commons.boot.interfaces.Environment;

public abstract class DecoratorFactory<E extends Environment, T extends Closeable> {
    private static final Logger log = LogManager.getLogger();

    public T create( E environment, JSONObject config ) {
        log.entry( config );
        if ( config.has( "inner" ) ) {
            if ( config.get( "inner" ) instanceof JSONArray ) {
                log.debug( "need to recurse (multiple inners) " + config.getString( "type" ) );
                List<T> inners = new ArrayList<T>();
                for( int i = 0; i < config.getJSONArray( "inner" ).length(); i++ ) {
                    inners.add( create( environment, config.getJSONArray( "inner" ).getJSONObject( i ) ) );
                }
                MultipleWrappingFactory<E, T> factory = findMultipleWrappingFactory( config.getString( "type" ), environment );
                try {
                    return log.exit( factory.wrap( environment, inners, config ) );
                } catch ( Exception e ) {
                    try {
                        for( T inner : inners ) {
                            inner.close();
                        }
                    } catch ( Exception e2 ) {
                        log.warn( "Things broke even more!", e2 );
                    }
                    throw e;
                }
            } else {
                log.debug( "need to recurse " + config.getString( "type" ) );
                T inner = create( environment, config.getJSONObject( "inner" ) );
                WrappingFactory<E, T> factory = findWrappingFactory( config.getString( "type" ), environment );
                try {
                    return log.exit( factory.wrap( environment, inner, config ) );
                } catch ( Exception e ) {
                    try {
                        inner.close();
                    } catch ( Exception e2 ) {
                        log.warn( "Things broke even more!", e2 );
                    }
                    throw e;
                }
            }
        } else {
            log.debug( "using basic factory" );
            BasicFactory<E, T> basicFactory = findBasicFactory( config.getString( "type" ), environment );
            return log.exit( basicFactory.create( environment, config ) );
        }
    }

    private boolean envIsAssignableToAll( List<Class<? super E>> envTypes, E envToCheck ) {
        for( Class<? super E> envType : envTypes ) {
            if ( !envType.isAssignableFrom( envToCheck.getClass() ) ) {
                return false;
            }
        }
        return true;
    }

    private MultipleWrappingFactory<E, T> findMultipleWrappingFactory( String type, E env ) {
        for( MultipleWrappingFactory<E, T> wrappingFactory : getMultipleWrappingFactories() ) {
            if ( type.equalsIgnoreCase( wrappingFactory.handledType() )
                    && envIsAssignableToAll( wrappingFactory.getRequiredEnvironments(), env ) ) {
                return wrappingFactory;
            }
        }
        throw new IllegalArgumentException( "Cannot find factory for type " + type + " with inner class and current environment" );
    }

    private WrappingFactory<E, T> findWrappingFactory( String type, E env ) {
        for( WrappingFactory<E, T> wrappingFactory : getWrappingFactories() ) {
            if ( type.equalsIgnoreCase( wrappingFactory.handledType() )
                    && envIsAssignableToAll( wrappingFactory.getRequiredEnvironments(), env ) ) {
                return wrappingFactory;
            }
        }
        throw new IllegalArgumentException( "Cannot find factory for type " + type + " with inner class and current environment" );
    }

    private BasicFactory<E, T> findBasicFactory( String type, E env ) {
        for( BasicFactory<E, T> basicFactory : getBasicFactories() ) {
            if ( type.equalsIgnoreCase( basicFactory.handledType() )
                    && envIsAssignableToAll( basicFactory.getRequiredEnvironments(), env ) ) {
                return basicFactory;
            }
        }
        throw new IllegalArgumentException( "Cannot find factory for type " + type + "with current environment" );
    }

    protected abstract Collection<MultipleWrappingFactory<E, T>> getMultipleWrappingFactories();
    protected abstract Collection<WrappingFactory<E, T>> getWrappingFactories();
    protected abstract Collection<BasicFactory<E, T>> getBasicFactories();
}
