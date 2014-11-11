package com.goodgame.profiling.rewrite_framework.drain.serial.neverfail;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.goodgame.profiling.commons.model.Metric;
import com.goodgame.profiling.rewrite_framework.core.drain.Drain;

public final class SerialNeverFailDrainTest {

    @SuppressWarnings( "unchecked" )
    @Test
    public void testOutputSingleOneException() throws IOException {
        Drain inner1 = mock( Drain.class );
        Drain inner2 = mock( Drain.class );

        Metric input = new Metric( "dontCare", 1, 1d );
        Drain subject = new SerialNeverFailDrain( Arrays.asList( inner1, inner2 ) );
        doThrow( new RuntimeException() ).when( inner1 ).output( any( List.class ) );

        subject.output( Arrays.asList( input ) );

        subject.flushRemainingBuffers();
        subject.close();

        verify( inner1 ).output( Arrays.asList( input ) );
        verify( inner2 ).output( Arrays.asList( input ) );
    }

    @Test
    public void testFlush() throws IOException {
        Drain inner1 = mock( Drain.class );
        Drain inner2 = mock( Drain.class );

        doThrow( new RuntimeException() ).when( inner1 ).flushRemainingBuffers();

        Drain subject = new SerialNeverFailDrain( Arrays.asList( inner1, inner2 ) );

        subject.flushRemainingBuffers();
        subject.close();

        verify( inner1 ).flushRemainingBuffers();
        verify( inner2 ).flushRemainingBuffers();
    }

    @Test
    public void testClose() throws IOException {
        Drain inner1 = mock( Drain.class );
        Drain inner2 = mock( Drain.class );

        doThrow( new RuntimeException() ).when( inner1 ).close();

        Drain subject = new SerialNeverFailDrain( Arrays.asList( inner1, inner2 ) );

        subject.close();

        verify( inner1 ).close();
        verify( inner2 ).close();
    }
}
