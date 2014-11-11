package com.goodgame.profiling.rewrite_framework.output.rewriter.modifier.replace;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.goodgame.profiling.rewrite_framework.output.rewriter.modifier.Modifier;

public class Replacer implements Modifier {
	private final Pattern[] patterns;
	private final String[] replacements;

	public Replacer( Map<String, String> uncompiledReplacements ) {
		patterns = new Pattern[ uncompiledReplacements.size() ];
		replacements = new String[ uncompiledReplacements.size() ];
		int compileCount = 0;
		for ( Map.Entry<String, String> uncompiledReplacement : uncompiledReplacements.entrySet() ) {
			patterns[compileCount] = Pattern.compile(uncompiledReplacement.getKey(), Pattern.LITERAL);
			replacements[compileCount] = Matcher.quoteReplacement( uncompiledReplacement.getValue() );
			compileCount++;
		}
	}

	@Override
	public String modify( String input ) {
		for ( int ii = 0; ii < patterns.length; ii++ ) {
			input = patterns[ii].matcher( input ).replaceAll( replacements[ii] );
		}
		return input;
	}
}
