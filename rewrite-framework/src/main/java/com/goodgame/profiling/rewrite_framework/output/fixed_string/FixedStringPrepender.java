package com.goodgame.profiling.rewrite_framework.output.fixed_string;

import com.goodgame.profiling.rewrite_framework.core.output.Match;
import com.goodgame.profiling.rewrite_framework.core.output.generator.MetricNameGenerator;

public final class FixedStringPrepender implements MetricNameGenerator {
	private final String prefix;
	private final MetricNameGenerator innerGenerator;

	public FixedStringPrepender( String prefix, MetricNameGenerator innerGenerator ) {
		this.prefix = prefix;
		this.innerGenerator = innerGenerator;
	}

	@Override
	public String generateMetricName( String sourceId, Match match ) {
		return prefix + innerGenerator.generateMetricName( sourceId, match );
	}
}
