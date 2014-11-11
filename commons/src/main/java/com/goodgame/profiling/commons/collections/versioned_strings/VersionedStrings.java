package com.goodgame.profiling.commons.collections.versioned_strings;

public interface VersionedStrings extends Iterable< VersionedString > {
    VersionedString getCurrentString();
    void insert( String newestVersion );
    void removeLastString();

    int countVersions();
}
