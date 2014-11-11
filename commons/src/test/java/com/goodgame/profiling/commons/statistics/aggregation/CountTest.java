package com.goodgame.profiling.commons.statistics.aggregation;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CountTest {

	private static final double DELTA = 0.01;

	@Test
	public void testConsumeValue() {
		CountAggregation subject = new CountAggregation();
		assertEquals( 0, subject.getAggregatedValue(), DELTA );
		subject.consumeValue( 42 );
		assertEquals( 1, subject.getAggregatedValue(), DELTA );
		subject.consumeValue( 23 );
		assertEquals( 2, subject.getAggregatedValue(), DELTA );
		subject.consumeValue( 128 );
		assertEquals( 3, subject.getAggregatedValue(), DELTA );
	}

}
