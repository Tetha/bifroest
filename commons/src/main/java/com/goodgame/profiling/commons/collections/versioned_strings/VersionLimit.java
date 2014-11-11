package com.goodgame.profiling.commons.collections.versioned_strings;

public class VersionLimit extends AbstractVersionedStringsWrapper {
    private final int limit;

    public VersionLimit( VersionedStrings wrappedVersionedStrings, int limit ) {
        super( wrappedVersionedStrings );
        this.limit = limit;
    }

    @Override
    public void insert(String newestVersion) {
        super.insert( newestVersion );
        while ( limit < countVersions() ) {
            removeLastString();
        }
    }
}
