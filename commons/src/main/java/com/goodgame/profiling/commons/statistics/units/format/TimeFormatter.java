package com.goodgame.profiling.commons.statistics.units.format;

import java.util.Arrays;

import com.goodgame.profiling.commons.statistics.units.SI_PREFIX;
import com.goodgame.profiling.commons.statistics.units.TIME_UNIT;

public class TimeFormatter extends WithSubUnitsFormatter<TIME_UNIT> {

	private final SI_PREFIX prefix;

	public TimeFormatter( int nrComponents, SI_PREFIX prefix, TIME_UNIT inputUnit ) {
		super( nrComponents, inputUnit, Arrays.asList( TIME_UNIT.values() ) );
		this.prefix = prefix;
	}

	public TimeFormatter( int nrComponents, TIME_UNIT inputUnit ) {
		this( nrComponents, SI_PREFIX.ONE, inputUnit );
	}

	public TimeFormatter( int nrComponents ) {
		this( nrComponents, TIME_UNIT.SECOND );
	}

	public TimeFormatter() {
		this( 2 );
	}

	public TimeFormatter( SI_PREFIX prefix, TIME_UNIT inputUnit ) {
		this( 2, prefix, inputUnit );
	}

	@Override
	public String format( double value ) {
		return super.format( value * prefix.getMultiplier() );
	}

	@Override
	protected String onNoSubunitsLeft( double remainingValue, int remainingComponents ) {
		SiFormatterWithSubUnits subformatter = new SiFormatterWithSubUnits( remainingComponents );

		return subformatter.format( remainingValue );
	}

	private class SiFormatterWithSubUnits extends WithSubUnitsFormatter<SI_PREFIX> {

		public SiFormatterWithSubUnits( int nrComponents ) {
			super( nrComponents, SI_PREFIX.ONE, Arrays.asList( SI_PREFIX.values() ), "s" );
		}

		@Override
		protected String onNoSubunitsLeft( double remainingValue, int remainingComponents ) {
			return "";
		}
	}

}
