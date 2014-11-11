package com.goodgame.profiling.rewrite_framework.output.fixed_string;

import java.util.Collections;
import java.util.List;

import org.json.JSONObject;
import org.kohsuke.MetaInfServices;

import com.goodgame.profiling.commons.boot.interfaces.Environment;
import com.goodgame.profiling.rewrite_framework.core.output.generator.MetricNameGenerator;
import com.goodgame.profiling.rewrite_framework.core.output.generator.WrappingNameGeneratorFactory;

@MetaInfServices
public final class FixedStringPrependerFactory<E extends Environment>
implements WrappingNameGeneratorFactory<E> {

	@Override
	public String handledType() {
		return "prefix string";
	}

	@Override
	public MetricNameGenerator wrap( E environment, MetricNameGenerator inner, JSONObject config ) {
		return new FixedStringPrepender( config.getString( "prefix" ), inner );
	}

	@Override
	public List<Class<? super E>> getRequiredEnvironments() {
		return Collections.emptyList();
	}
}
