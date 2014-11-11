package com.goodgame.profiling.graphite_retentions;

import java.util.Objects;

public final class RetentionLevel implements Comparable<RetentionLevel> {

    private final String name;
    private final long frequency;
    private final long blocks;
    private final long blockSize;
    private final String next;

    public RetentionLevel( String name, long frequency, long blocks, long blockSize, String next ) {
        if ( blockSize % frequency != 0 ) {
            throw new IllegalArgumentException( String.format( "frequency(%d) doesn't divide blockSize(%d)", frequency, blockSize ) );
        }
        this.name = name;
        this.frequency = frequency;
        this.blocks = blocks;
        this.blockSize = blockSize;
        this.next = next;
    }

    public String name() {
        return name;
    }

    public long frequency() {
        return frequency;
    }

    public long blocks() {
        return blocks;
    }

    public long blockSize() {
        return blockSize;
    }

    public long size() {
        return blockSize * blocks;
    }

    public long indexOf( long timestamp ) {
        return timestamp / blockSize;
    }
    
    public String next(){
    	return next;
    }

    @Override
    public int hashCode() {
        return Objects.hash( name, frequency, blocks, blockSize );
    }

    @Override
    public boolean equals( Object obj ) {
        if ( this == obj ) {
            return true;
        } else if ( obj == null ) {
            return false;
        } else if ( !( obj instanceof RetentionLevel ) ) {
            return false;
        }
        RetentionLevel level = (RetentionLevel) obj;
        boolean result = true;
        result &= name.equals( level.name );
        result &= ( frequency == level.frequency );
        result &= ( blocks == level.blocks );
        result &= ( blockSize == level.blockSize );
        return result;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder( name );
        result.append( '[' );
        result.append( blocks ).append( "blocks@" ).append( blockSize ).append( "seconds" );
        result.append( ";freq=" ).append( frequency );
        result.append( ']' );
        return result.toString();
    }

    @Override
    public int compareTo( RetentionLevel level ) {
        int compare = Long.compare( this.frequency, level.frequency );
        if ( compare != 0 ) {
            return compare;
        }
        compare = Long.compare( this.blockSize, level.blockSize );
        if ( compare != 0 ) {
            return compare;
        }
        compare = Long.compare( this.blocks, level.blocks );
        return compare;
    }

}
