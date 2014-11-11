package com.goodgame.profiling.rewrite_framework.drain.serial.failfirst;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.goodgame.profiling.commons.model.Metric;
import com.goodgame.profiling.rewrite_framework.core.drain.Drain;

public class SerialFailFirstDrainTest {
    @Test
    public void testOutputSingleNoException() throws IOException {
        Drain inner1 = mock( Drain.class );
        Drain inner2 = mock( Drain.class );

        Metric myMetric = new Metric( "a", 1, 1.1 );

        Drain subject = new SerialFailFirstDrain( Arrays.asList( inner1, inner2 ) );

        subject.output( Arrays.asList( myMetric ) );

        verify( inner1 ).output( Arrays.asList( myMetric ) );
        verify( inner2 ).output( Arrays.asList( myMetric ) );
        
        subject.close();
    }

    @Test
    public void testOutputMultipleNoException() throws IOException {
        Drain inner1 = mock( Drain.class );
        Drain inner2 = mock( Drain.class );

        List<Metric> myMetrics = Arrays.asList( new Metric( "a", 1, 1.1 ), new Metric( "b", 2, 2.2 ) );

        Drain subject = new SerialFailFirstDrain( Arrays.asList( inner1, inner2 ) );

        subject.output( myMetrics );

        verify( inner1 ).output( myMetrics );
        verify( inner2 ).output( myMetrics );
        
        subject.close();
    }

    @SuppressWarnings( "unchecked" )
    @Test(expected=RuntimeException.class)
    public void testOutputSingleWithException() throws IOException {
        Drain inner1 = mock( Drain.class );
        Drain inner2 = mock( Drain.class );
        doThrow( new RuntimeException() ).when( inner1 ).output( any( List.class ) );

        Metric myMetric = new Metric( "a", 1, 1.1 );

        Drain subject = new SerialFailFirstDrain( Arrays.asList( inner1, inner2 ) );

        try {
            subject.output( Arrays.asList( myMetric ) );
        } finally {
            verify( inner2, never() ).output( any( List.class ) );
            
            subject.close();
        }
    }

    @SuppressWarnings( "unchecked" )
    @Test(expected=RuntimeException.class)
    public void testOutputMultipleWithException() throws IOException {
        Drain inner1 = mock( Drain.class );
        Drain inner2 = mock( Drain.class );
        doThrow( new RuntimeException() ).when( inner1 ).output( any( List.class ) );

        List<Metric> myMetrics = Arrays.asList( new Metric( "a", 1, 1.1 ), new Metric( "b", 2, 2.2 ) );

        Drain subject = new SerialFailFirstDrain( Arrays.asList( inner1, inner2 ) );

        try {
            subject.output( myMetrics );
        } finally {
            verify( inner2, never() ).output( any( List.class ) );
            
            subject.close();
        }
    }
}
