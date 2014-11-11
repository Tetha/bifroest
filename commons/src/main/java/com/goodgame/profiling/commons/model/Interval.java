package com.goodgame.profiling.commons.model;

import java.util.Objects;

public final class Interval {

    private final long start;
    private final long end;

    public Interval( long from, long to ) {
        this.start = Math.min( from, to );
        this.end = Math.max( from, to );
    }

    public long start() {
        return start;
    }

    public long end() {
        return end;
    }

    public boolean contains( long value ) {
        return ( value >= start ) && ( value < end );
    }

    public boolean intersects( Interval interval ) {
        return ( interval.start < end || interval.end > start );
    }

    @Override
    public int hashCode() {
        return Objects.hash( start, end );
    }

    @Override
    public boolean equals( Object obj ) {
        if ( this == obj ) {
            return true;
        } else if ( obj == null ) {
            return false;
        } else if ( !( obj instanceof Interval ) ) {
            return false;
        }
        Interval interval = (Interval) obj;
        return ( start == interval.start ) && ( end == interval.end );
    }

    @Override
    public String toString() {
        return "[" + start + ";" + end + "]";
    }

}
