package com.goodgame.profiling.rewrite_framework.core.output;

import java.util.Collection;

import com.goodgame.profiling.rewrite_framework.core.output.generator.MetricNameGenerator;

public interface RewriteRule<I> {
	Match match( I input );
	Collection<MetricNameGenerator> generators();
}
