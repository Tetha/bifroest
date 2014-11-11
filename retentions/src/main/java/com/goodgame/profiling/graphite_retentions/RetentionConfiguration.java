package com.goodgame.profiling.graphite_retentions;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

import com.goodgame.profiling.commons.statistics.aggregation.ValueAggregation;
import com.goodgame.profiling.commons.model.Metric;

public interface RetentionConfiguration {
    ValueAggregation findFunctionForMetric( String name );
    Optional<RetentionTable> findWriteTableForMetric( String name, long timestamp );
    Optional<List<RetentionLevel>> findReadLevelsForMetric(String name);
    Optional<RetentionLevel> findWriteLevelForMetric(String name);
    Optional<RetentionLevel> getNextLevel(RetentionLevel level);
    Optional<RetentionLevel> getLevelForName(String levelname);
    OptionalLong getNameRetentionForName(String name);
    Collection<RetentionLevel> getAllLevels();
	List<RetentionLevel> getTopologicalSort();
    

    default ValueAggregation findFunctionForMetric( Metric metric ) {
        return findFunctionForMetric( metric.name() );
    }

    default Optional<RetentionTable> findWriteTableForMetric( Metric metric ) {
        return findWriteTableForMetric( metric.name(), metric.timestamp() );
    }
    
    default Optional<List<RetentionLevel>> findReadLevelsForMetric(Metric metric){
    	return findReadLevelsForMetric(metric.name());
    }
    
    default Optional<RetentionLevel> findWriteLevelForMetric(Metric metric){
    	return findWriteLevelForMetric(metric.name());
    }
    
    default Optional<RetentionLevel> getLevelForMetric(Metric metric){
    	return getLevelForName(metric.name());
    }
    
    default OptionalLong getNameRetentionForMetric(Metric metric){
    	return getNameRetentionForName(metric.name());
    }
	
}
