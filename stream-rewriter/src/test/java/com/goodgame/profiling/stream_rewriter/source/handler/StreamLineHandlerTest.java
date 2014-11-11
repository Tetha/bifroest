package com.goodgame.profiling.stream_rewriter.source.handler;

import org.junit.Test;

import com.goodgame.profiling.commons.model.Metric;
import com.goodgame.profiling.rewrite_framework_test.integration.ExpectationBasedDrain;

public class StreamLineHandlerTest {

    @Test
    public void test() {
        ExpectationBasedDrain drain = new ExpectationBasedDrain( new Metric("some.metric", 1234567890, 4d ) );

        StreamLineHandler subject = new StreamLineHandler( drain );

        subject.handleUnit( "some.metric 4 1234567890" );

        drain.flushRemainingBuffers();
        drain.close();
    }
}
