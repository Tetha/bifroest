package com.goodgame.profiling.commons.util;

import java.util.Objects;

import java.util.function.Consumer;
import java.util.function.Function;

public final class Either<GOOD, BAD> {
    private final GOOD gv;
    private final BAD bv;

    private Either( GOOD a, BAD b ) {
        this.gv = a;
        this.bv = b;
    }

    public <GOOD2, BAD2> Either<GOOD2, BAD2> apply( Function<GOOD, GOOD2> goodMapping, Function<BAD, BAD2> badMapping ) {
        return new Either<GOOD2, BAD2>( gv == null ? null : goodMapping.apply( gv ), bv == null ? null : badMapping.apply( bv ) );
    }

    public <GOOD2> Either<GOOD2, BAD> then( Function<GOOD, Either<GOOD2, BAD>> f ) {
        if ( gv != null ) {
            return f.apply( gv );
        } else {
            return new Either<GOOD2, BAD>( null, bv );
        }
    }

    public <GOOD2> Either<GOOD2, BAD> mapGoodValue( Function<GOOD, GOOD2> mapping ) {
        return this.<GOOD2, BAD> apply( mapping, i -> i );
    }

    public <BAD2> Either<GOOD, BAD2> mapBadValue( Function<BAD, BAD2> mapping ) {
        return apply( i -> i, mapping );
    }

    public void consume( Consumer<GOOD> goodConsumer, Consumer<BAD> badConsumer ) {
        if ( gv != null )
            goodConsumer.accept( gv );
        if ( bv != null )
            badConsumer.accept( bv );
    }

    public boolean isGood() {
        return gv != null;
    }

    public boolean isBad() {
        return bv != null;
    }

    public boolean equals( Object o ) {
        if ( o == null )
            return false;
        if ( o == this )
            return true;

        if ( !( o instanceof Either ) ) {
            return false;
        }
        Either<?, ?> co = (Either<?, ?>) o;
        return Objects.equals( gv, co.gv ) && Objects.equals( bv, co.bv );
    }

    public int hashCode() {
        return Objects.hashCode( gv ) - Objects.hashCode( bv );
    }

    public String toString() {
        return String.format( "Either(state=%1s,value=%2s)", isGood() ? "Good" : "Bad", isGood() ? gv : bv );
    }

    public static <GOOD, BAD> Either<GOOD, BAD> ofGoodValue( GOOD a ) {
        return new Either<GOOD, BAD>( Objects.requireNonNull( a ), null );
    }

    public static <GOOD, BAD> Either<GOOD, BAD> ofBadValue( BAD b ) {
        return new Either<GOOD, BAD>( null, Objects.requireNonNull( b ) );
    }
}
