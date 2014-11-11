package com.goodgame.profiling.commons.model;

import java.util.Objects;

public final class Metric {

    private final String name;
    private final long timestamp;
    private final double value;

    public Metric( String name, long timestamp, double value ) {
        this.name = Objects.requireNonNull( name );
        this.timestamp = timestamp;
        this.value = value;
    }

    public String name() {
        return name;
    }

    public long timestamp() {
        return timestamp;
    }

    public double value() {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hash( name, timestamp, value );
    }

    @Override
    public boolean equals( Object obj ) {
        if ( this == obj ) {
            return true;
        } else if ( obj == null ) {
            return false;
        } else if ( !( obj instanceof Metric ) ) {
            return false;
        }
        Metric metric = (Metric) obj;
        boolean result = true;
        result &= name.equals( metric.name );
        result &= ( timestamp == metric.timestamp );
        result &= ( value == metric.value );
        return result;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder( "Metric" );
        result.append( " [" );
        result.append( "name=" ).append( name ).append( ", " );
        result.append( "timestamp=" ).append( timestamp ).append( ", " );
        result.append( "value=" ).append( value ).append( "," );
        result.append( "value[hex]=").append( Double.toHexString(value) ).append( "]" );
        return result.toString();
    }

}
