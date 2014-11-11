package com.goodgame.profiling.rewrite_framework.output.rewriter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.text.StrBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.goodgame.profiling.rewrite_framework.core.output.Match;
import com.goodgame.profiling.rewrite_framework.core.output.generator.MetricNameGenerator;

public final class Rewriter implements MetricNameGenerator {
	private static final Logger log =
			LogManager.getLogger();

	private final List<Chunk> chunks;


	public Rewriter( String pattern ) {
		chunks = new LinkedList<Chunk>();

		List<String> patternChunks = splitRegardingBraces( pattern );
		for ( String chunk : patternChunks ) {
			log.trace( "Handling chunk " + chunk );

			if ( chunk.startsWith( "$" ) ) {
				chunks.add( new SpecialChunk( chunk ) );
			} else {
				chunks.add( new ReturnChunk( chunk ) );
			}
		}
	}

	@Override
	public String generateMetricName( String host, Match match ) {
		log.entry( match );
		StrBuilder result = new StrBuilder();

		for ( Chunk chunk : chunks ) {
			result.appendSeparator( '.' );
			result.append( chunk.apply( match ) );
		}
		return log.exit( result.toString() );
	}

	private static List<String> splitRegardingBraces( String pattern ) {
		List<String> chunks = new ArrayList<>();
		StringBuilder chunk = new StringBuilder();
		int openBraces = 0;
		for ( int i = 0; i < pattern.length(); i++ ) {
			char currentChar = pattern.charAt( i );
			log.trace( "{}, {}", currentChar, openBraces );
			if ( currentChar == '.') {
				if ( openBraces > 0 ) {
					chunk.append( currentChar );
				} else {
					log.trace("Finishing chunk " + chunk.toString() );
					chunks.add( chunk.toString() );
					chunk = new StringBuilder();
				}
			} else if ( currentChar == '}' ) {
				chunk.append( currentChar );
				openBraces --;
				if ( 0 < openBraces ) {
					throw new IllegalStateException( "Too many closing braces at char " + i + " in " + pattern );
				}
			} else if ( currentChar == '{' ) {
				chunk.append( currentChar );
				openBraces ++;
			} else {
				chunk.append( currentChar );
			}
		}

		if ( 0 < chunk.length() ) {
			chunks.add( chunk.toString() );
		}
		return chunks;
	}
}
