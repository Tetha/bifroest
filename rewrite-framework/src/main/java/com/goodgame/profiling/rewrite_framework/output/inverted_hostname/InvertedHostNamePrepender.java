package com.goodgame.profiling.rewrite_framework.output.inverted_hostname;

import static com.goodgame.profiling.rewrite_framework.source.HostnameInverter.invertHostname;

import com.goodgame.profiling.rewrite_framework.core.output.Match;
import com.goodgame.profiling.rewrite_framework.core.output.generator.MetricNameGenerator;

public final class InvertedHostNamePrepender implements MetricNameGenerator {
    private final MetricNameGenerator inner;

    public InvertedHostNamePrepender( MetricNameGenerator extendedGenerator ) {
        this.inner = extendedGenerator;
    }

    @Override
    public String generateMetricName( String host, Match match ) {
        return invertHostname( host ) + "." + inner.generateMetricName( host, match );
    }
}
