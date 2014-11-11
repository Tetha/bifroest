package com.goodgame.profiling.rewrite_framework.core.config;

import java.util.Collection;

import com.goodgame.profiling.rewrite_framework.core.drain.Drain;
import com.goodgame.profiling.rewrite_framework.core.output.RewriteRule;
import com.goodgame.profiling.rewrite_framework.core.source.SourceSet;

public interface FetchConfiguration<I, U> {
	Collection<SourceSet<U>> sources();
	Drain drain();
	Collection<RewriteRule<I>> rules();
}
