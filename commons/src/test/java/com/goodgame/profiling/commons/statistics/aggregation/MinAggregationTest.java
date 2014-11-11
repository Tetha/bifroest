package com.goodgame.profiling.commons.statistics.aggregation;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MinAggregationTest {

	private static final double DELTA = 0.1;

	@Test
	public void checkValueAggregation() {
		ValueAggregation subject = new MinAggregation();

		assertEquals( Double.MAX_VALUE, subject.getAggregatedValue(), DELTA );
		subject.consumeValue( 10 );
		assertEquals( 10, subject.getAggregatedValue(), DELTA );
		subject.consumeValue( 20 );
		assertEquals( 10, subject.getAggregatedValue(), DELTA );
		subject.consumeValue( 5 );
		assertEquals( 5, subject.getAggregatedValue(), DELTA );
	}

}
