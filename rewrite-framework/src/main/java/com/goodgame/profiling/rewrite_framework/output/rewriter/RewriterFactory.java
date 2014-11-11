package com.goodgame.profiling.rewrite_framework.output.rewriter;

import java.util.Collections;
import java.util.List;

import org.json.JSONObject;
import org.kohsuke.MetaInfServices;

import com.goodgame.profiling.commons.boot.interfaces.Environment;
import com.goodgame.profiling.rewrite_framework.core.output.generator.BasicNameGeneratorFactory;
import com.goodgame.profiling.rewrite_framework.core.output.generator.MetricNameGenerator;

@MetaInfServices
public final class RewriterFactory<E extends Environment> implements BasicNameGeneratorFactory<E> {

	public String handledType() {
		return "rewrite from match";
	}

	@Override
	public MetricNameGenerator create(E environment, JSONObject configuration) {
		return new Rewriter( configuration.getString( "metric" ) );
	}

	@Override
	public List<Class<? super E>> getRequiredEnvironments() {
		return Collections.emptyList();
	}
}
