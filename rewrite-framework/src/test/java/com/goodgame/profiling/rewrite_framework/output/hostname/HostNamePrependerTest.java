package com.goodgame.profiling.rewrite_framework.output.hostname;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mockito.Mock;

import com.goodgame.profiling.commons.boot.interfaces.Environment;
import com.goodgame.profiling.rewrite_framework.core.output.Match;
import com.goodgame.profiling.rewrite_framework.core.output.generator.MetricNameGenerator;
import com.goodgame.profiling.rewrite_framework.core.output.generator.WrappingNameGeneratorFactory;

public final class HostNamePrependerTest {
    @Mock
    private Environment env;

    private static final class FixedString implements MetricNameGenerator {
        @Override
        public String generateMetricName( String host, Match match ) {
            return "CPU.load";
        }
    }

    @Test
    public void testIPHosts() {
        WrappingNameGeneratorFactory<Environment> subject = new HostNamePrependerFactory<>();
        MetricNameGenerator created = subject.wrap( env, new FixedString(), null );
        assertEquals( "10.20.224.10.CPU.load", created.generateMetricName( "10.20.224.10", null ) );
    }

    @Test
    public void testNamedHosts() {
        WrappingNameGeneratorFactory<Environment> subject = new HostNamePrependerFactory<>();
        MetricNameGenerator created = subject.wrap( env, new FixedString(), null );
        assertEquals( "game-nl02.empire.nl.ggs-net.com.CPU.load", created.generateMetricName( "game-nl02.empire.nl.ggs-net.com", null ) );
    }
}
