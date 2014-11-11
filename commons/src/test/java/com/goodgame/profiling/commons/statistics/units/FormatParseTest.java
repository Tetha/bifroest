package com.goodgame.profiling.commons.statistics.units;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.goodgame.profiling.commons.statistics.units.format.TimeFormatter;
import com.goodgame.profiling.commons.statistics.units.format.UnitFormatter;
import com.goodgame.profiling.commons.statistics.units.parse.TimeUnitParser;
import com.goodgame.profiling.commons.statistics.units.parse.UnitParser;

public class FormatParseTest {

	private static final int NUM_TESTS = 100;

	private Random random;

	@Before
	public void initRandom() {
		random = new Random();
	}

	@Ignore @Test
	public void randomizedFormatAndParse() {
		for ( int i = 0; i < NUM_TESTS; i++ ) {

			SI_PREFIX prefix = SI_PREFIX.values()[random.nextInt( SI_PREFIX.values().length )];
			TIME_UNIT unit = TIME_UNIT.values()[random.nextInt( TIME_UNIT.values().length )];
			UnitFormatter formatter = new TimeFormatter( prefix, unit );
			UnitParser parser = new TimeUnitParser( prefix, unit );

			double value = random.nextDouble();
			String formatted = formatter.format( value );
			double result = parser.parse( formatted ).doubleValue();
			System.out.println();
			System.out.println( value + " -> " + formatted + " -> " + result );
			System.out.println( prefix + " " + unit);
			assertEquals( value, result, 0.1 );

		}
	}

}
