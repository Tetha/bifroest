package com.goodgame.profiling.rewrite_framework.drain.empty;

import java.util.List;

import com.goodgame.profiling.commons.model.Metric;
import com.goodgame.profiling.rewrite_framework.core.drain.AbstractBasicDrain;

public class EmptyDrain extends AbstractBasicDrain {
	public EmptyDrain() {
	}

	@Override
	public void output( List<Metric> metrics ) {
		// Whoosh
	}
}
