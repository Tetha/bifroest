package com.goodgame.profiling.commons.statistics.units;

public enum TIME_UNIT implements Scales {
	YEAR( 365 * 24 * 60 * 60, "y" ), DAY( 24 * 60 * 60, "d" ), HOUR( 60 * 60, "h" ), MINUTE( 60, "m" ), SECOND( 1, "s" );

	private double multiplier;
	private String symbol;

	private TIME_UNIT( double multiplier, String symbol ) {
		this.multiplier = multiplier;
		this.symbol = symbol;
	}

	@Override
	public double getMultiplier() {
		return multiplier;
	}

	@Override
	public String getSymbol() {
		return symbol;
	}

}
