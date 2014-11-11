package com.goodgame.profiling.commons.collections.versioned_strings;


public class DuplicateFreeVersionedStrings extends AbstractVersionedStringsWrapper {
    public DuplicateFreeVersionedStrings( VersionedStrings wrappedVersionedStrings ) {
        super( wrappedVersionedStrings );
    }

    @Override
    public void insert(String newestVersion) {
        if ( !getCurrentString().content().equals( newestVersion ) ) {
            super.insert( newestVersion );
        }
    }
}
