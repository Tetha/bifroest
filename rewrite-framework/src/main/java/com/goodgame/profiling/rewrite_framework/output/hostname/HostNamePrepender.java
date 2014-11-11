package com.goodgame.profiling.rewrite_framework.output.hostname;

import com.goodgame.profiling.rewrite_framework.core.output.Match;
import com.goodgame.profiling.rewrite_framework.core.output.generator.MetricNameGenerator;

public class HostNamePrepender implements MetricNameGenerator {
    private final MetricNameGenerator inner;

    public HostNamePrepender( MetricNameGenerator extendedGenerator ) {
        this.inner = extendedGenerator;
    }

    @Override
    public String generateMetricName( String host, Match match ) {
        return host + "." + inner.generateMetricName( host, match );
    }
}
