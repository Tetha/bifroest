package com.goodgame.profiling.rewrite_framework.output.rewriter;

import com.goodgame.profiling.rewrite_framework.core.output.Match;

public interface Chunk {
	public String apply( Match match );
}
