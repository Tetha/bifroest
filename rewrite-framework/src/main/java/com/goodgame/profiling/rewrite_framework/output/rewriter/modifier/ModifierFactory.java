package com.goodgame.profiling.rewrite_framework.output.rewriter.modifier;

public interface ModifierFactory {
	String handledType();
	Modifier create( String modifierParams );
}
