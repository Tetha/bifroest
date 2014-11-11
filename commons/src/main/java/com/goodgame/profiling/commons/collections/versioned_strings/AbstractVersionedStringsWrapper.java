package com.goodgame.profiling.commons.collections.versioned_strings;

import java.util.Iterator;


public abstract class AbstractVersionedStringsWrapper implements VersionedStrings {
    private final VersionedStrings wrappedVersionedStrings;

    public AbstractVersionedStringsWrapper( VersionedStrings versionedStrings ) {
        this.wrappedVersionedStrings = versionedStrings;
    }

    @Override
    public Iterator<VersionedString> iterator() {
        return wrappedVersionedStrings.iterator();
    }

    @Override
    public VersionedString getCurrentString() {
        return wrappedVersionedStrings.getCurrentString();
    }

    @Override
    public void insert(String newestVersion) {
        wrappedVersionedStrings.insert( newestVersion );
    }

    @Override
    public void removeLastString() {
        wrappedVersionedStrings.removeLastString();
    }

    @Override
    public int countVersions() {
        return wrappedVersionedStrings.countVersions();
    }
}
