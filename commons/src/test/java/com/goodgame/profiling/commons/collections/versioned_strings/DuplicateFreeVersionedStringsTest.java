package com.goodgame.profiling.commons.collections.versioned_strings;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class DuplicateFreeVersionedStringsTest {
    @Mock
    private VersionedStrings wrappedVersionedStrings;

    @Before
    public void setupMocks() {
        MockitoAnnotations.initMocks( this ); 
        when( wrappedVersionedStrings.getCurrentString() ).thenReturn( new VersionedString( 1235, "current string" ) );
    }

    @Test
    public void testDuplicateWrite() {
        VersionedStrings subject = new DuplicateFreeVersionedStrings( wrappedVersionedStrings );
        subject.insert( "current string" );
        verify( wrappedVersionedStrings, never() ).insert( "current string" );
    }

    @Test
    public void testNewWrite() {
        VersionedStrings subject = new DuplicateFreeVersionedStrings( wrappedVersionedStrings );
        subject.insert( "new string" );
        verify( wrappedVersionedStrings, times(1) ).insert( "new string" );
    }
}
