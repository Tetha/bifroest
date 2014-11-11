package com.goodgame.profiling.rewrite_framework.core.output;

public interface Match {
	boolean matches();
	String apply( String source, int group );
}
