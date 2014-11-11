package com.goodgame.profiling.commons.statistics.aggregation;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class WindowAverageTest {

	private static final double DELTA = 0.01;

	@Test
	public void testConsumeValue() {
		ValueAggregation subject = new WindowAverageAggregation( 3 );
		assertEquals( 0, subject.getAggregatedValue(), DELTA );
		subject.consumeValue( 100 );
		assertEquals( 100, subject.getAggregatedValue(), DELTA );
		subject.consumeValue( 200 );
		assertEquals( 150, subject.getAggregatedValue(), DELTA );
		subject.consumeValue( 100 );
		assertEquals( 133.33, subject.getAggregatedValue(), DELTA );
		subject.consumeValue( 0 );
		assertEquals( 100, subject.getAggregatedValue(), DELTA );
		subject.consumeValue( 200 );
		assertEquals( 100, subject.getAggregatedValue(), DELTA );
	}

}
