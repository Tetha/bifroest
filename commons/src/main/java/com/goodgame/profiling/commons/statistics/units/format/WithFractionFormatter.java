package com.goodgame.profiling.commons.statistics.units.format;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

import com.goodgame.profiling.commons.statistics.units.Scales;

public abstract class WithFractionFormatter< T extends Scales > implements UnitFormatter {

	private final int significantDigits;
	private final T inputUnit;
	private final List<T> allUnits;

	public WithFractionFormatter( int significantDigits, T inputUnit, List<T> allUnits ) {
		this.significantDigits = significantDigits;
		this.inputUnit = inputUnit;
		this.allUnits = allUnits;
	}

	private T getAppropriatePrefix( double value ) {
		for ( T t : allUnits ) {
			if ( value / t.getMultiplier() >= 1 ) {
				return t;
			}
		}

		return allUnits.get( allUnits.size() - 1 );
	}

	private int getFractionDigits( double value ) {
		assert ( value >= 1 );
		assert ( value < 1000 );

		int fractionDigits;

		if ( value >= 100 ) {
			fractionDigits = significantDigits - 3;
		} else if ( value >= 10 ) {
			fractionDigits = significantDigits - 2;
		} else {
			fractionDigits = significantDigits - 1;
		}
		fractionDigits = Math.max( fractionDigits, 0 );
		return fractionDigits;
	}

	private DecimalFormat getDecimalFormat( int fractionDigits ) {
		DecimalFormat format = new DecimalFormat();

		format.setMaximumFractionDigits( fractionDigits );
		format.setMinimumFractionDigits( fractionDigits );

		format.setDecimalFormatSymbols( new DecimalFormatSymbols( Locale.US ) );

		return format;
	}

	@Override
	public String format( double value ) {
		double normalizedValue = value * inputUnit.getMultiplier();

		T outputUnit = getAppropriatePrefix( normalizedValue );
		double skaledValue = normalizedValue / outputUnit.getMultiplier();

		return getDecimalFormat( getFractionDigits( skaledValue ) ).format( skaledValue ) + outputUnit.getSymbol();
	}

}
