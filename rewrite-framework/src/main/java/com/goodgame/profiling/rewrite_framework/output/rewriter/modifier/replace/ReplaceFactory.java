package com.goodgame.profiling.rewrite_framework.output.rewriter.modifier.replace;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.MetaInfServices;

import com.goodgame.profiling.rewrite_framework.output.rewriter.modifier.Modifier;
import com.goodgame.profiling.rewrite_framework.output.rewriter.modifier.ModifierFactory;

@MetaInfServices
public final class ReplaceFactory implements ModifierFactory {
	private static final Logger log = LogManager.getLogger();

	@Override
	public String handledType() {
		return "replace";
	}

	@Override
	public Modifier create( String params ) {
		log.entry("ReplaceFactory.create(" + params + ")");

		Map<String, String> result = new LinkedHashMap<String, String>();
		String[] replacements = params.split( "," );
		for ( String replacement : replacements ) {
			String[] replacementChunks = replacement.split( "=>" );
			result.put( replacementChunks[0].trim(), replacementChunks[1].trim() );
		}
		return new Replacer( result );
	}
}
