package com.goodgame.profiling.commons.statistics.units.parse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.goodgame.profiling.commons.statistics.units.SI_PREFIX;
import com.goodgame.profiling.commons.statistics.units.Scales;
import com.goodgame.profiling.commons.statistics.units.TIME_UNIT;

public class TimeUnitParser implements UnitParser {

	private final SI_PREFIX basePrefix;
	private final TIME_UNIT baseUnit;

	private final Pattern regex;

	public TimeUnitParser( SI_PREFIX basePrefix, TIME_UNIT baseUnit ) {
		this.basePrefix = basePrefix;
		this.baseUnit = baseUnit;
		this.regex = computeRegex();
	}

	public TimeUnitParser( TIME_UNIT baseUnit ) {
		this( SI_PREFIX.ONE, baseUnit );
	}

	public TimeUnitParser() {
		this( SI_PREFIX.ONE, TIME_UNIT.SECOND );
	}

	private static Pattern computeRegex() {
		StringBuilder regex = new StringBuilder( "(?:" );
		// Number
		regex.append( "(?<number>[+-]?\\p{Digit}+(\\.\\p{Digit}+)?(?:[eE][+-]?\\p{Digit}+)?)" );
		// Prefix
		regex.append( "(?<prefix>" ).append( scalesToSymbolRegex( SI_PREFIX.values() ) ).append( ")?" );
		// Unit
		regex.append( "(?<unit>" ).append( scalesToSymbolRegex( TIME_UNIT.values() ) ).append( ")" );
		// Repeat
		regex.append( ")+" );
		return Pattern.compile( regex.toString() );
	}

	private static StringBuilder scalesToSymbolRegex( Scales... scales ) {
		StringBuilder regex = new StringBuilder();
		regex.append( '[' );
		for ( int i = 0; i < scales.length; i++ ) {
			String symbol = scales[i].getSymbol();
			if ( symbol.length() == 1 ) {
				regex.append( symbol );
			}
		}
		regex.append( ']' );
		for ( int i = 0; i < scales.length; i++ ) {
			String symbol = scales[i].getSymbol();
			if ( symbol.length() > 1 ) {
				regex.append( '|' ).append( symbol );
			}
		}
		return regex;
	}

	@Override
	public Number parse( String string ) {
		double result = 0;
		Matcher matcher = regex.matcher( string );
		while ( matcher.find() ) {
			double number = Double.parseDouble( matcher.group( "number" ) );
			String prefixString = matcher.group( "prefix" );
			SI_PREFIX prefix = SI_PREFIX.ONE;
			if ( prefixString != null ) {
				for ( int i = 0; i < SI_PREFIX.values().length; i++ ) {
					if ( SI_PREFIX.values()[i].getSymbol().equals( prefixString ) ) {
						prefix = SI_PREFIX.values()[i];
					}
				}
			}
			String unitString = matcher.group( "unit" );
			TIME_UNIT unit = TIME_UNIT.SECOND;
			for ( int i = 0; i < TIME_UNIT.values().length; i++ ) {
				if ( TIME_UNIT.values()[i].getSymbol().equals( unitString ) ) {
					unit = TIME_UNIT.values()[i];
				}
			}

			result += number * ( prefix.getMultiplier() / basePrefix.getMultiplier() ) * ( unit.getMultiplier() / baseUnit.getMultiplier() );
		}
		return result;
	}

}
