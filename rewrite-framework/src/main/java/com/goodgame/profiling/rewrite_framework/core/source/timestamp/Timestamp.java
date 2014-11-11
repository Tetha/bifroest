package com.goodgame.profiling.rewrite_framework.core.source.timestamp;

public abstract class Timestamp {

    private Timestamp next;
    private final int priority;

    public Timestamp( Timestamp next, int priority ) {
        this.next = next;
        this.priority = priority;
    }

    public Timestamp append( Timestamp next ) {
        this.next = next;
        return this;
    }

    public Timestamp next() {
        return next;
    }

    public int priority() {
        return priority;
    }

    public abstract long getTime( long currentTime, String sourceId );

    @Override
    public String toString() {
        return getClass().getName() + ( next == null ? "" : " > " + next.toString() );
    }

}
