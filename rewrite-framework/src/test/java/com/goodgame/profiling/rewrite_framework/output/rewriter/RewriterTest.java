package com.goodgame.profiling.rewrite_framework.output.rewriter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import com.goodgame.profiling.rewrite_framework.core.output.Match;
import com.goodgame.profiling.rewrite_framework.core.output.generator.MetricNameGenerator;

public class RewriterTest {
    @Test
    public void testGeneration() {
        String graphName = "eventsinout.foo";
        String datasourceName = "events_bar_baz";

        final Matcher graphMatch = Pattern.compile("eventsinout.(\\w+)").matcher(graphName);
        final Matcher datasourceMatch = Pattern.compile("events_(\\w+)_(\\w+)").matcher(datasourceName);

        assertTrue(graphMatch.matches());
        assertTrue(datasourceMatch.matches());

        Match match = new Match() {

            @Override
            public boolean matches() {
                return graphMatch.matches() && datasourceMatch.matches();
            }

            @Override
            public String apply(String source, int group) {
                if (source.equalsIgnoreCase("A")) {
                    return graphMatch.group(group);
                } else if (source.equalsIgnoreCase("B")) {
                    return datasourceMatch.group(group);
                } else {
                    return "";
                }
            }
        };

        String pattern = "Events.${A,1}.${B,2}.${B,1}";

        MetricNameGenerator subject = new Rewriter(pattern);

        assertEquals("Events.foo.baz.bar", subject.generateMetricName(null, match));

        subject.close();
    }
}
