package com.goodgame.profiling.rewrite_framework.core.drain;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;

import com.goodgame.profiling.commons.boot.interfaces.Environment;
import com.goodgame.profiling.rewrite_framework.core.BasicFactory;
import com.goodgame.profiling.rewrite_framework.core.DecoratorFactory;
import com.goodgame.profiling.rewrite_framework.core.MultipleWrappingFactory;
import com.goodgame.profiling.rewrite_framework.core.WrappingFactory;

public class DrainFactory<E extends Environment> extends DecoratorFactory<E, Drain> {
    @SuppressWarnings( "rawtypes" )
    private static final ServiceLoader<BasicDrainFactory> basicFactories = ServiceLoader.load( BasicDrainFactory.class );
    @SuppressWarnings( "rawtypes" )
    private static final ServiceLoader<DrainWrapperFactory> wrappingFactories = ServiceLoader.load( DrainWrapperFactory.class );
    @SuppressWarnings( "rawtypes" )
    private static final ServiceLoader<DrainMultipleWrapperFactory> multipleWrappingFactories = ServiceLoader.load( DrainMultipleWrapperFactory.class );

    @SuppressWarnings( "unchecked" )
    @Override
    protected Collection<BasicFactory<E, Drain>> getBasicFactories() {
        List<BasicFactory<E, Drain>> factories = new LinkedList<>();

        for( BasicDrainFactory<E> factory : basicFactories ) {
            factories.add( factory );
        }

        return factories;
    }

    @SuppressWarnings( "unchecked" )
    @Override
    protected Collection<WrappingFactory<E, Drain>> getWrappingFactories() {
        List<WrappingFactory<E, Drain>> factories = new LinkedList<>();

        for( DrainWrapperFactory<E> factory : wrappingFactories ) {
            factories.add( factory );
        }

        return factories;
    }

    @SuppressWarnings( "unchecked" )
    @Override
    protected Collection<MultipleWrappingFactory<E, Drain>> getMultipleWrappingFactories() {
        List<MultipleWrappingFactory<E, Drain>> factories = new LinkedList<>();

        for( DrainMultipleWrapperFactory<E> factory : multipleWrappingFactories ) {
            factories.add( factory );
        }

        return factories;
    }
}
