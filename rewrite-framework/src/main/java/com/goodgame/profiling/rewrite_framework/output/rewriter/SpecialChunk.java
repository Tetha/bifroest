package com.goodgame.profiling.rewrite_framework.output.rewriter;

import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.goodgame.profiling.rewrite_framework.core.output.Match;
import com.goodgame.profiling.rewrite_framework.output.rewriter.modifier.Modifier;
import com.goodgame.profiling.rewrite_framework.output.rewriter.modifier.ModifierCreator;

public class SpecialChunk implements Chunk {
	private static final Logger log = LogManager.getLogger();

	// mostly final in this case means: I only assign them once,
	// but java can't infer that, because I'm assigning stuff in a
	// rather crazy and complicated parser
	private String match; // mostly final
	private int requestedGroup; // mostly final
	private final List<Modifier> modifiers;
	private String rest = ""; // mostly final

	public SpecialChunk( String partOfPattern ) {
		int nesting = 0;

		log.entry( partOfPattern );


		State state = State.MATCH_NAME;
		StringBuilder chunk = new StringBuilder();

		String modifierName = null;
		modifiers = new LinkedList<Modifier>();
		for ( int i = "${".length() ; i < partOfPattern.length(); i++ ) {
			char currentChar = partOfPattern.charAt( i );
			log.trace("State: " + state + ", char=" + currentChar);
			if ( currentChar == ',' ) {
				switch ( state ) {
				case MATCH_NAME:
					state = State.GROUP_ID;
					match = chunk.toString();
					chunk = new StringBuilder();
					continue;

				case GROUP_ID:
					state = State.MODIFIER_NAME;
					requestedGroup = Integer.valueOf( chunk.toString() );
					chunk = new StringBuilder();
					continue;

				case AFTER_MODIFIER_PARAMS:
					state = State.MODIFIER_NAME;
					continue;

				default:
					chunk.append( currentChar );
					continue;
				}
			} else if ( currentChar == '(' ) {
				if ( nesting++ == 0 ) {
					switch ( state ) {
					case MODIFIER_NAME:
						state = State.MODIFIER_PARAMS;
						modifierName = chunk.toString();
						chunk = new StringBuilder();
						continue;

					default:
						chunk.append( currentChar );
					}
				} else {
					chunk.append( currentChar );
				}
			} else if ( currentChar == ')' ) {
				if ( --nesting == 0 ) {
					switch ( state ) {
					case MODIFIER_PARAMS:
						state = State.AFTER_MODIFIER_PARAMS;
						modifiers.add( ModifierCreator.create( modifierName, chunk.toString() ) );
						chunk = new StringBuilder();
						continue;
					default:
						chunk.append( currentChar );
						continue;
					}
				} else {
					chunk.append( currentChar );
				}
			} else if ( currentChar == '}' ) {
				if ( state == State.GROUP_ID ) {
					requestedGroup = Integer.valueOf( chunk.toString() );
				} else {
					if ( i < partOfPattern.length() -1  ) {
						rest = partOfPattern.substring(i+1, partOfPattern.length() );
					}
				}
				break;
			} else {
				chunk.append( currentChar );
			}
		}
	}

	private enum State {
		MATCH_NAME,
		GROUP_ID,
		MODIFIER_NAME,
		MODIFIER_PARAMS,
		AFTER_MODIFIER_PARAMS
	}

	@Override
	public String apply( Match sourceMatch ) {
		log.entry( sourceMatch );
		String value = sourceMatch.apply( match, requestedGroup );
		for ( Modifier m : modifiers ) {
			value = m.modify( value );
		}
		return log.exit( value + rest );
	}
}
