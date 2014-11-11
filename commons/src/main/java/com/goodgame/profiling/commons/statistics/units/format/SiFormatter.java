package com.goodgame.profiling.commons.statistics.units.format;

import java.util.Arrays;

import com.goodgame.profiling.commons.statistics.units.SI_PREFIX;

public class SiFormatter extends WithFractionFormatter<SI_PREFIX> {

	public SiFormatter( int significantDigits, SI_PREFIX inputUnit ) {
		super( significantDigits, inputUnit, Arrays.asList( SI_PREFIX.values() ) );
	}

	public SiFormatter( int significantDigits ) {
		this( significantDigits, SI_PREFIX.ONE );
	}

	public SiFormatter() {
		this( 3 );
	}

}
