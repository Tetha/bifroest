package com.goodgame.profiling.rewrite_framework.output.fixed_string;

import static org.junit.Assert.assertEquals;

import org.json.JSONObject;
import org.junit.Test;
import org.mockito.Mock;

import com.goodgame.profiling.commons.boot.interfaces.Environment;
import com.goodgame.profiling.rewrite_framework.core.output.Match;
import com.goodgame.profiling.rewrite_framework.core.output.generator.MetricNameGenerator;

public final class FixedStringPrependerTest {
	@Mock private Environment env;
	
	@Test
	public void testCreationAndFunction() {
		JSONObject config = new JSONObject().put( "prefix", "something " );
		FixedStringPrependerFactory<Environment> subject = new FixedStringPrependerFactory<>();
		MetricNameGenerator result = subject.wrap( env, new StaticString(), config );
		assertEquals( "something awesome", result.generateMetricName( null, null ) );
	}

	private static final class StaticString implements MetricNameGenerator {
		@Override
		public String generateMetricName( String sourceId, Match match ) {
			return "awesome";
		}
	}
}
