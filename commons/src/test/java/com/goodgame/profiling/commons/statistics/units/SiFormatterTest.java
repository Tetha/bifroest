package com.goodgame.profiling.commons.statistics.units;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.goodgame.profiling.commons.statistics.units.format.SiFormatter;
import com.goodgame.profiling.commons.statistics.units.format.UnitFormatter;

public class SiFormatterTest {

	@Test
	public void testFormat1() {
		UnitFormatter formatter = new SiFormatter( 3, SI_PREFIX.ONE );
		assertEquals( "1.00", formatter.format( 1 ) );
	}

	@Test
	public void testFormat2() {
		UnitFormatter formatter = new SiFormatter( 3, SI_PREFIX.ONE );
		assertEquals( "2.00", formatter.format( 2 ) );
	}

	@Test
	public void testFormatOneThousandK() {
		UnitFormatter formatter = new SiFormatter( 0, SI_PREFIX.KILO );
		assertEquals( "1M", formatter.format( 1000 ) );
	}

	@Test
	public void testFormatKeepAllIntegerDigits() {
		UnitFormatter formatter = new SiFormatter( 1, SI_PREFIX.ONE );
		assertEquals( "123m", formatter.format( 0.123 ) );
	}

	@Test
	public void testFormatBigAndSmall() {
		UnitFormatter formatter = new SiFormatter( 2, SI_PREFIX.NANO );
		assertEquals( "1.2", formatter.format( 1234567890 ) );
	}

}
