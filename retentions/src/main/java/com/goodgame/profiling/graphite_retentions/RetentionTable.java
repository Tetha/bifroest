package com.goodgame.profiling.graphite_retentions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.goodgame.profiling.commons.model.Interval;
import com.goodgame.profiling.graphite_retentions.RetentionConfiguration;

public final class RetentionTable implements Comparable<RetentionTable> {

    // cassandra table names are strictly alphanumerical.
    // Hence, we cannot use something silly like "-" to separate parts inside
    // a table name, and have to use a very sane solution like the one below.
    public static final String SEPARATOR_OF_MADNESS = "0X0";
    public static final Pattern TABLE_REGEX = Pattern.compile( "g" + "(?<level>\\w+)" + SEPARATOR_OF_MADNESS + "(?<block>\\d+)", Pattern.CASE_INSENSITIVE );

    //private final RetentionStrategy strategy;
    private final RetentionLevel level;
    private final long block;

    public RetentionTable( String tablename, RetentionConfiguration retentions ) {
        Matcher matcher = TABLE_REGEX.matcher( tablename );
        if ( matcher.matches() ) {
            String levelname = matcher.group( "level" ).toLowerCase();
            this.block = Long.parseLong( matcher.group( "block" ) );
            Optional <RetentionLevel> optLevel = retentions.getLevelForName(levelname);
            if(optLevel.isPresent()){
            	this.level = optLevel.get();
            }
            else{
            	throw new IllegalArgumentException( "tablename: <" + tablename + ">, levelname: <" + levelname + ">" );
            }
        } else {
            throw new IllegalArgumentException( "Table " + tablename + " doesn't match format." );
        }
    }

    public RetentionTable(RetentionLevel level, long block ) {
        //this.strategy = strategy;
        this.level = level;
        this.block = block;

        if ( tableName().length() > 32 ) {
            throw new IllegalStateException( "table name too long" );
        }

        if ( !StringUtils.isAlphanumeric( tableName() ) ) {
            throw new IllegalStateException( "table name is not alphanumeric" );
        }
    }

//    public RetentionStrategy strategy() {
//        return strategy;
//    }

    public RetentionLevel level() {
        return level;
    }

    public long block() {
        return block;
    }

    public boolean contains( long timestamp ) {
        long start = block * level.blockSize();
        long end = start + level.blockSize();
        return ( timestamp >= start ) && ( timestamp < end );
    }

    public Interval getInterval() {
        long start = block * level.blockSize();
        long end = start + level.blockSize();
        return new Interval( start, end );
    }

    public String tableName() {
        StringBuilder result = new StringBuilder( "g" );
        //result.append( strategy.name() );
        result.append( level.name() );
        result.append( SEPARATOR_OF_MADNESS ).append( block );
        return result.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(level, block );
    }

    @Override
    public boolean equals( Object obj ) {
        if ( this == obj ) {
            return true;
        } else if ( obj == null ) {
            return false;
        } else if ( !( obj instanceof RetentionTable ) ) {
            return false;
        }
        RetentionTable table = (RetentionTable) obj;
        boolean result = true;
       // result &= strategy.equals( table.strategy );
        result &= level.equals( table.level );
        result &= ( block == table.block );
        return result;
    }

    @Override
    public String toString() {
        DateFormat format = new SimpleDateFormat( "yyyy-MM-dd | HH:mm:ss " );
        Date start = new Date( block * level.blockSize() * 1000 );
        Date end = new Date( ( block + 1 ) * level.blockSize() * 1000 );
        StringBuilder result = new StringBuilder( "RetentionTable [ " );
        result.append( "Table=" ).append( tableName() ).append( ", " );
        //result.append( "Strategy=" ).append( strategy.name() ).append( ", " );
        result.append( "Level=" ).append( level.name() ).append( ", " );
        result.append( "BlockIndex=" ).append( block ).append( ", " );
        result.append( "StartTime=" ).append( format.format( start ) ).append( ", " );
        result.append( "EndTime=" ).append( format.format( end ) );
        result.append( "]" );
        return result.toString();
    }

    @Override
    public int compareTo( RetentionTable table ) {
        int compare = this.level.compareTo( table.level );
        if ( compare != 0 ) {
            return compare;
        }
        compare = (int) ( table.block() - this.block() );
        return compare;
    }

}
