package com.goodgame.profiling.rewrite_framework.output.rewriter;

import com.goodgame.profiling.rewrite_framework.core.output.Match;

public class ReturnChunk implements Chunk {
	private final String chunk;

	public ReturnChunk( String partOfPattern ) {
		this.chunk = partOfPattern;
	}

	@Override
	public String apply( Match match ) {
		return chunk;
	}
}
