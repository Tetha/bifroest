package com.goodgame.profiling.commons.collections.versioned_strings;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class VersionLimitTest {
    @Mock
    private VersionedStrings wrappedVersionedStrings;

    @Before
    public void setupMocks() {
        MockitoAnnotations.initMocks( this ); 
    }

    @Test
    public void testWritesUnderLimit() {
        VersionedStrings limit = new VersionLimit( wrappedVersionedStrings, 3 );
        
        when( wrappedVersionedStrings.countVersions() ).thenReturn( 2 );
        limit.insert( "qux" );

        verify( wrappedVersionedStrings, times(1) ).insert( "qux" );
        verify( wrappedVersionedStrings, never() ).removeLastString();
    }

    @Test
    public void testWritesOverLimit() {
        VersionedStrings limit = new VersionLimit( wrappedVersionedStrings, 3 );
        
        when( wrappedVersionedStrings.countVersions() ).thenReturn( 4 ).thenReturn( 3 );
        limit.insert( "qux" );

        verify( wrappedVersionedStrings, times(1) ).insert( "qux" );
        verify( wrappedVersionedStrings, times(1) ).removeLastString();
    }
}
