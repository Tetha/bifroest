package com.goodgame.profiling.commons.collections.versioned_strings;

import java.util.Objects;

public final class VersionedString {
    private long timeCreated;
    private String content;

    public VersionedString( long timeCreated, String content ) {
        Objects.requireNonNull( content );
        this.timeCreated = timeCreated;
        this.content = content;
    }

    public long timeCreated() {
        return timeCreated;
    }

    public String content() {
        return content;
    }

    @Override
    public int hashCode() {
        return Objects.hash( timeCreated, content );
    }

    @Override
    public boolean equals( Object other ) {
        if ( this == other ) return true;
        if ( other == null ) return false;
        if ( this.getClass() != other.getClass() ) return false;
        VersionedString ovs = (VersionedString) other;
        return Objects.equals( ovs.timeCreated, timeCreated )
               && Objects.equals( ovs.content, content );
    }

    @Override
    public String toString() {
        return "{ timeCreated : \"" + timeCreated + "\", content : \"" + content + "\" }";
    }
}
