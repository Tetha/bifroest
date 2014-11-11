package com.goodgame.profiling.rewrite_framework.core.output.generator;

import java.io.Closeable;

import com.goodgame.profiling.rewrite_framework.core.output.Match;

public interface MetricNameGenerator extends Closeable {
	String generateMetricName( String host, Match match );
	default void close () {} ;
}
