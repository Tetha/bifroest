package com.goodgame.profiling.commons.statistics.aggregation;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TotalAverageTest {

	private static final double DELTA = 0.01;

	@Test
	public void testConsumeValueAverages() {
		TotalAverageAggregation subject = new TotalAverageAggregation();
		assertEquals( 0, subject.getAggregatedValue(), DELTA );
		subject.consumeValue( 9 );
		assertEquals( 9, subject.getAggregatedValue(), DELTA );
		subject.consumeValue( 1 );
		assertEquals( 5, subject.getAggregatedValue(), DELTA );
		subject.consumeValue( 2 );
		assertEquals( 4, subject.getAggregatedValue(), DELTA );
		subject.consumeValue( 4 );
		assertEquals( 4, subject.getAggregatedValue(), DELTA );
	}

}