package com.goodgame.profiling.rewrite_framework.core.source.handler;

import com.goodgame.profiling.rewrite_framework.core.config.FetchConfiguration;
import com.goodgame.profiling.rewrite_framework.core.source.Source;

public interface SourceHandlerFactory<I, U> {
	Class<U> handledUnit();
	Class<I> handledInput();
	SourceUnitHandler<U> create( Source<U> source, FetchConfiguration<I, U> fetchConfig );
}
