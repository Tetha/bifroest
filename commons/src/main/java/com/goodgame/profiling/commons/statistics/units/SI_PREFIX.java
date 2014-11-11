package com.goodgame.profiling.commons.statistics.units;

public enum SI_PREFIX implements Scales {
	YOTTA( "Y" ), ZETTA( "Z" ), EXA( "E" ), PETA( "P" ), TERA( "T" ), GIGA( "G" ), MEGA( "M" ), KILO( "k" ), ONE( "" ), MILLI( "m" ), MICRO( "u" ), NANO( "n" ), PICO(
			"p" ), FEMTO( "f" ), ATTO( "a" ), ZEPTO( "z" ), YOCTO( "y" );

	private String symbol;

	private SI_PREFIX( String symbol ) {
		this.symbol = symbol;
	}

	@Override
	public double getMultiplier() {
		return Math.pow( 1000d, 8 - this.ordinal() );
	}

	@Override
	public String getSymbol() {
		return symbol;
	}

}
