package com.goodgame.profiling.rewrite_framework.statistics;

import com.goodgame.profiling.rewrite_framework.core.output.RewriteRule;

public class RewriteRuleApplied {

	private final String sourceId;
	private final RewriteRule<?> rewriteRule;

	public RewriteRuleApplied( String sourceId, RewriteRule<?> rewriteRule ) {
		this.sourceId = sourceId;
		this.rewriteRule = rewriteRule;
	}

	public String sourceId() {
		return sourceId;
	}

	public RewriteRule<?> rewriteRule() {
		return rewriteRule;
	}

}
