package com.goodgame.profiling.rewrite_framework.core.output.generator;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;

import com.goodgame.profiling.commons.boot.interfaces.Environment;
import com.goodgame.profiling.rewrite_framework.core.BasicFactory;
import com.goodgame.profiling.rewrite_framework.core.DecoratorFactory;
import com.goodgame.profiling.rewrite_framework.core.MultipleWrappingFactory;
import com.goodgame.profiling.rewrite_framework.core.WrappingFactory;

public class MetricNameGeneratorFactory<E extends Environment> extends DecoratorFactory<E, MetricNameGenerator> {
    @SuppressWarnings( "rawtypes" )
    private static final ServiceLoader<BasicNameGeneratorFactory> basicFactories = ServiceLoader.load( BasicNameGeneratorFactory.class );
    @SuppressWarnings( "rawtypes" )
    private static final ServiceLoader<WrappingNameGeneratorFactory> wrappingFactories = ServiceLoader.load( WrappingNameGeneratorFactory.class );
    @SuppressWarnings( "rawtypes" )
    private static final ServiceLoader<MultipleWrappingNameGeneratorFactory> multipleWrappingFactories = ServiceLoader.load( MultipleWrappingNameGeneratorFactory.class );

    @SuppressWarnings( "unchecked" )
    @Override
    protected Collection<BasicFactory<E, MetricNameGenerator>> getBasicFactories() {
        List<BasicFactory<E, MetricNameGenerator>> factories = new LinkedList<>();

        for( BasicNameGeneratorFactory<E> factory : basicFactories ) {
            factories.add( factory );
        }

        return factories;
    }

    @SuppressWarnings( "unchecked" )
    @Override
    protected Collection<WrappingFactory<E, MetricNameGenerator>> getWrappingFactories() {
        List<WrappingFactory<E, MetricNameGenerator>> factories = new LinkedList<>();

        for( WrappingNameGeneratorFactory<E> factory : wrappingFactories ) {
            factories.add( factory );
        }

        return factories;
    }

    @SuppressWarnings( "unchecked" )
    @Override
    protected Collection<MultipleWrappingFactory<E, MetricNameGenerator>> getMultipleWrappingFactories() {
        List<MultipleWrappingFactory<E, MetricNameGenerator>> factories = new LinkedList<>();

        for( MultipleWrappingNameGeneratorFactory<E> factory : multipleWrappingFactories ) {
            factories.add( factory );
        }

        return factories;
    }
}
