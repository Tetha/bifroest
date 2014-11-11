package com.goodgame.profiling.commons.statistics.duration;

import java.time.Instant;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.TabularData;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.goodgame.profiling.commons.statistics.aggregation.CountAggregation;
import com.goodgame.profiling.commons.statistics.aggregation.MaxAggregation;
import com.goodgame.profiling.commons.statistics.aggregation.TotalAverageAggregation;
import com.goodgame.profiling.commons.statistics.aggregation.WindowAverageAggregation;
import com.goodgame.profiling.commons.statistics.aggregation.WindowMaxAggregation;
import com.goodgame.profiling.commons.statistics.calllog.CallLog;
import com.goodgame.profiling.commons.statistics.jmx.TabularDataFromValueAggregationMap;
import com.goodgame.profiling.commons.statistics.storage.MetricStorage;

public class PartitionedDurationStatistics implements PartitionedDurationStatisticsMBean {

    private static final Logger log = LogManager.getLogger();

    private final Map<String, CountAggregation> numberOfCalls;
    private final Map<String, TotalAverageAggregation> averages;
    private final Map<String, WindowAverageAggregation> averagesAcrossLast;
    private final Map<String, MaxAggregation> maxes;
    private final Map<String, WindowMaxAggregation> maxesAcrossLast;
    private final CallLog callLog;
    private final int windowSize;

    private final CountAggregation totalCallCount;
    private final TotalAverageAggregation totalAverageDuration;
    private final WindowAverageAggregation totalAverageAcrossLast;
    private final MaxAggregation totalMaxDuration;
    private final WindowMaxAggregation totalMaxAcrossLast;

    public PartitionedDurationStatistics( int windowSize, int callLogSize, int expectedPartitionCount ) {
        callLog = new CallLog( callLogSize );
        this.windowSize = windowSize;
        numberOfCalls = new HashMap<String, CountAggregation>( expectedPartitionCount );
        averages = new HashMap<String, TotalAverageAggregation>( expectedPartitionCount );
        averagesAcrossLast = new HashMap<String, WindowAverageAggregation>( expectedPartitionCount );
        maxes = new HashMap<String, MaxAggregation>( expectedPartitionCount );
        maxesAcrossLast = new HashMap<>( expectedPartitionCount );

        totalCallCount = new CountAggregation();
        totalAverageDuration = new TotalAverageAggregation();
        totalAverageAcrossLast = new WindowAverageAggregation( windowSize );
        totalMaxDuration = new MaxAggregation();
        totalMaxAcrossLast = new WindowMaxAggregation( windowSize );
    }

    public void handleCall( Instant start, String partitionName, Instant end ) {
        Duration d = Duration.between( start, end );
        long duration = d.toNanos();

        getCountFor( partitionName ).consumeValue( 1 );
        getAverage( partitionName ).consumeValue( duration );
        getMax( partitionName ).consumeValue( duration );
        getAverageAcrossLast( partitionName ).consumeValue( duration );
        getMaxAcrossLast( partitionName ).consumeValue( duration );
        callLog.logCall( start.toEpochMilli(), partitionName, end.toEpochMilli() );

        totalCallCount.consumeValue( 1 );
        totalAverageDuration.consumeValue( duration );
        totalAverageAcrossLast.consumeValue( duration );
        totalMaxDuration.consumeValue( duration );
        totalMaxAcrossLast.consumeValue( duration );
    }

    private CountAggregation getCountFor( String command ) {
        if ( !numberOfCalls.containsKey( command ) ) {
            numberOfCalls.put( command, new CountAggregation() );
        }
        return numberOfCalls.get( command );
    }

    private TotalAverageAggregation getAverage( String pluginName ) {
        if ( !averages.containsKey( pluginName ) ) {
            averages.put( pluginName, new TotalAverageAggregation() );
        }
        return averages.get( pluginName );
    }

    private WindowAverageAggregation getAverageAcrossLast( String pluginName ) {
        if ( !averagesAcrossLast.containsKey( pluginName ) ) {
            averagesAcrossLast.put( pluginName, new WindowAverageAggregation( windowSize ) );
        }
        return averagesAcrossLast.get( pluginName );
    }

    private MaxAggregation getMax( String pluginName ) {
        if ( !maxes.containsKey( pluginName ) ) {
            maxes.put( pluginName, new MaxAggregation() );
        }
        return maxes.get( pluginName );
    }

    private WindowMaxAggregation getMaxAcrossLast( String pluginName ) {
        if ( !maxesAcrossLast.containsKey( pluginName ) ) {
            maxesAcrossLast.put( pluginName, new WindowMaxAggregation( windowSize ) );
        }
        return maxesAcrossLast.get( pluginName );
    }

    @Override
    public TabularData getCallLog() throws OpenDataException {
        return callLog.toJmxTable();
    }

    @Override
    public TabularData getCallCounts() throws OpenDataException {
        try {
            return new TabularDataFromValueAggregationMap( numberOfCalls ).withRowTypeCalled( "command_calls" ).withRowTypeDescribedAs( "Number of calls" )
                    .withTableTypeCalled( "call_counts" ).withTableTypeDescribedAs( "Number of calls for the different partitions" ).withKeysCalled( "command" )
                    .withKeysDescribedAs( "The executed command" ).withValuesCalled( "calls" ).withValuesDescribedAs( "Number of calls" ).buildData();
        } catch( OpenDataException e ) {
            log.error( "Cannot obtain data", e );
            throw e;
        }
    }

    @Override
    public TabularData getAverageDurations() throws OpenDataException {
        try {
            return new TabularDataFromValueAggregationMap( averages ).withRowTypeCalled( "average_duration" ).withRowTypeDescribedAs( "Average Duration" )
                    .withTableTypeCalled( "average_durations" ).withTableTypeDescribedAs( "Average durations for the different partitions" )
                    .withKeysCalled( "command" ).withKeysDescribedAs( "The executed command" ).withValuesCalled( "duration" )
                    .withValuesDescribedAs( "Duration of the call" ).buildData();
        } catch( OpenDataException e ) {
            log.error( "Cannot obtain data", e );
            throw e;
        }
    }

    @Override
    public TabularData getAverageDurationsFromLastCalls() throws OpenDataException {
        try {
            return new TabularDataFromValueAggregationMap( averagesAcrossLast ).withRowTypeCalled( "average_duration" )
                    .withRowTypeDescribedAs( "Average Duration of one call" ).withTableTypeCalled( "average_durations" )
                    .withTableTypeDescribedAs( "Average durations for the different partitions" ).withKeysCalled( "command" )
                    .withKeysDescribedAs( "The executed command" ).withValuesCalled( "duration" ).withValuesDescribedAs( "Duration of the call" ).buildData();
        } catch( OpenDataException e ) {
            log.error( "Cannot obtain data", e );
            throw e;
        }
    }

    @Override
    public TabularData getMaxDurations() throws OpenDataException {
        try {
            return new TabularDataFromValueAggregationMap( maxes ).withRowTypeCalled( "max_duration" )
                    .withRowTypeDescribedAs( "Maximum Duration of one command call" ).withTableTypeCalled( "max_durations" )
                    .withTableTypeDescribedAs( "Maximum durations of calls for the various partitions" ).withKeysCalled( "plugin" )
                    .withKeysDescribedAs( "The called plugin" ).withValuesCalled( "duration" )
                    .withValuesDescribedAs( "Maximum duration of a call into this plugin" ).buildData();
        } catch( OpenDataException e ) {
            log.error( "Cannot obtain data", e );
            throw e;
        }
    }

    @Override
    public TabularData getMaxDurationsFromLastCalls() throws OpenDataException {
        try {
            return new TabularDataFromValueAggregationMap( maxesAcrossLast ).withRowTypeCalled( "max_duration" )
                    .withRowTypeDescribedAs( "Maximum Duration of one call" ).withTableTypeCalled( "max_durations" )
                    .withTableTypeDescribedAs( "Maximum durations for the different partitions" ).withKeysCalled( "command" )
                    .withKeysDescribedAs( "The executed command" ).withValuesCalled( "duration" ).withValuesDescribedAs( "Duration of the call" ).buildData();
        } catch( OpenDataException e ) {
            log.error( "Cannot obtain data", e );
            throw e;
        }
    }

    @Override
    public double getTotalCallCounts() {
        return totalCallCount.getAggregatedValue();
    }

    @Override
    public double getTotalAverageDuration() {
        return totalAverageDuration.getAggregatedValue();
    }

    @Override
    public double getTotalAverageDurationFromLastCalls() {
        return totalAverageAcrossLast.getAggregatedValue();
    }

    @Override
    public double getMaxDuration() {
        return totalMaxDuration.getAggregatedValue();
    }

    @Override
    public double getMaxDurationFromLastCalls() {
        return totalMaxAcrossLast.getAggregatedValue();
    }

    public void writeInto( MetricStorage storage ) {
        storage.store( "count", getTotalCallCounts() );
        storage.store( "avgduration", getTotalAverageDuration() );
        storage.store( "avgdurationwindow", getTotalAverageDurationFromLastCalls() );
        storage.store( "maxduration", getMaxDuration() );
        storage.store( "maxdurationwindow", getMaxDurationFromLastCalls() );

        for( Map.Entry<String, CountAggregation> entry : numberOfCalls.entrySet() ) {
            storage.getSubStorageCalled( entry.getKey() ).store( "count", entry.getValue().getAggregatedValue() );
        }
        for( Map.Entry<String, TotalAverageAggregation> entry : averages.entrySet() ) {
            storage.getSubStorageCalled( entry.getKey() ).store( "avgduration", entry.getValue().getAggregatedValue() );
        }
        for( Map.Entry<String, WindowAverageAggregation> entry : averagesAcrossLast.entrySet() ) {
            storage.getSubStorageCalled( entry.getKey() ).store( "avgdurationwindow", entry.getValue().getAggregatedValue() );
        }
        for( Map.Entry<String, MaxAggregation> entry : maxes.entrySet() ) {
            storage.getSubStorageCalled( entry.getKey() ).store( "maxduration", entry.getValue().getAggregatedValue() );
        }
        for( Map.Entry<String, WindowMaxAggregation> entry : maxesAcrossLast.entrySet() ) {
            storage.getSubStorageCalled( entry.getKey() ).store( "maxdurationwindow", entry.getValue().getAggregatedValue() );
        }
    }
}
