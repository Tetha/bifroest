package com.goodgame.profiling.graphite_retentions;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Pattern;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.commons.collections4.map.LinkedMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import com.goodgame.profiling.commons.statistics.aggregation.ValueAggregation;
import com.goodgame.profiling.commons.statistics.aggregation.ValueAggregationFactory;
import com.goodgame.profiling.commons.statistics.cache.CacheTracker;

public class MutableRetentionConfiguration implements RetentionConfiguration {
    private static final Logger log = LogManager.getLogger();
    private static final Marker AGGREGATION_CONFIG_MARKER = MarkerManager.getMarker( "AGGREGATION_CONFIG_MARKER" );

    private final Map<Pattern, String> functionMap;
    private final Map<Pattern, List<String>> readLevelsMap;
    private final Map<Pattern, String> writeLevelMap;
    private final Map<Pattern, Long> nameRetentionMap;
    
    private final Map<String, RetentionLevel> levels;
    
    private LinkedList<RetentionLevel> topologicalSort;
    
//    private final Map<String, RetentionStrategy> strategies;
    private static volatile CacheTracker tracker;

    private static ThreadLocal<FunctionMapCache> functionMapCache = new ThreadLocal<FunctionMapCache>() {
        protected FunctionMapCache initialValue() {
            log.trace( "Creating new FunctionMapCache" );
            if ( tracker == null ) {
                synchronized ( this ) {
                    if ( tracker == null ) {
                        tracker = CacheTracker.storingIn( "Caches", FunctionMapCache.NAME );
                    }
                }
            }
            return new FunctionMapCache( tracker );
        }
    };

    private static final Map<String, ValueAggregationFactory> aggregationFunctionFactories;

    static {
        // counted valueaggregations by hand and added some
        aggregationFunctionFactories = new HashMap<String, ValueAggregationFactory>( 10 );
        for ( ValueAggregationFactory factory : ServiceLoader.load( ValueAggregationFactory.class ) ) {
            aggregationFunctionFactories.put( factory.getFunctionName().toLowerCase(), factory );
        }
    }

    public MutableRetentionConfiguration() {
        // function map and strategy map need to preserve order.
        this.functionMap = new LinkedMap<>();
        this.readLevelsMap = new LinkedMap<>();
        this.writeLevelMap = new LinkedMap<>();
        this.nameRetentionMap = new LinkedMap<>();        

//        this.strategies = new HashMap<>();
        this.levels = new HashMap<>();
    }

    public void addFunctionEntry( String regex, String function ) {
    	log.trace( "Adding new functionEntry {} {}", regex, function );
        functionMap.put( Pattern.compile( regex ), function.toLowerCase() );
    }

    public void addReadLevelsEntry( String regex, List<String> names ) {
    	log.trace( "Adding new readLevelEntry {} {}", regex, names );
        readLevelsMap.put( Pattern.compile( regex ), names );
        
    }
    
    public void addWriteLevelEntry( String regex, String name ) {
    	log.trace( "Adding new writeLevelEntry {} {}", regex, name );
        writeLevelMap.put( Pattern.compile( regex ), name );
    }
    
    public void addNameRetentionEntry(String regex, long seconds){
    	log.trace( "Adding new nameRetentionEntry {} {}", regex, seconds );
    	nameRetentionMap.put( Pattern.compile( regex ), seconds);
    }
    
    public void addLevel(RetentionLevel level){
    	log.trace( "Adding new Level {}", level.name() );
    	levels.put( level.name(), level );
    }
    
    @Override
    public Optional<RetentionLevel> getNextLevel(RetentionLevel level){
    	log.entry(level);
    	RetentionLevel nextLevel = levels.get(level.next());
    	return log.exit(Optional.ofNullable(nextLevel));
    }
    
    public Optional<RetentionLevel> getLevelForName(String name){
    	log.entry(name);
    	RetentionLevel level = levels.get(name);
		return log.exit(Optional.ofNullable(level));
    }

    @Override
    public ValueAggregation findFunctionForMetric( String name ) {
        return log.exit(aggregationFunctionFactories.get( functionMapCache.get().get( name, this ) ).createAggregation());
    }

    private String findAggregationNameThroughATonOfRegexes( String name ) {
    	log.entry(name);
        for ( Entry<Pattern, String> entry : functionMap.entrySet() ) {
            if ( entry.getKey().matcher( name ).find() ) {
                if ( aggregationFunctionFactories.containsKey( entry.getValue() ) ) {
                    return log.exit(entry.getValue());
                } else {
                    log.warn( AGGREGATION_CONFIG_MARKER, entry.getValue() + " is not a supported aggregation method - using average" );
                    return log.exit("average");
                }
            }
        }
        log.warn( AGGREGATION_CONFIG_MARKER, "No aggregation function defined for " + name + " - using average" );
        return log.exit("average");
    }

    @Override
    public Optional<RetentionTable> findWriteTableForMetric( String name, long timestamp ) {
    	log.entry(name, timestamp);
        Optional<RetentionLevel> level = findWriteLevelForMetric(name);
        if(level.isPresent()){
        	return Optional.of(new RetentionTable( level.get() , level.get().indexOf(timestamp) ));
        }
        log.warn("No WriteTable found for {} {}", name, timestamp);
        return log.exit(Optional.empty());
    }

    private static final class FunctionMapCache {
        private static final String NAME = "RetentionFunctionMapCache";

        private final CacheTracker tracker;

        private WeakReference<MutableRetentionConfiguration> createdFromWeak;
        private LRUMap<String, String> cache;
        public FunctionMapCache( CacheTracker tracker ) {
            this.tracker = Objects.requireNonNull( tracker );
            this.createdFromWeak = new WeakReference<>( null );
            cache = new LRUMap<String, String>( 50 );
        } 

        public String get( String metricName, MutableRetentionConfiguration currentRetentionConfiguration ) {
            RetentionConfiguration createdFrom = createdFromWeak.get();

            // full configuration reloads invalidate this cache, 
            // but full configuration reloads create a whole new 
            // retention configuration object.
            //
            if ( createdFrom == currentRetentionConfiguration ) {
                String cachedFunctionName = cache.get( metricName );

                if ( cachedFunctionName == null ) {
                    return findFunctionWithoutCache( metricName, currentRetentionConfiguration );
                } else {
                    tracker.cacheHit( cache.size(), cache.maxSize() );
                    return log.exit(cachedFunctionName);
                }
            } else {
                // invalidate everything, the world has changed
                cache.clear();
                createdFromWeak = new WeakReference<>( currentRetentionConfiguration );
                return findFunctionWithoutCache( metricName, currentRetentionConfiguration );
            }
        }

        private String findFunctionWithoutCache( String metricName, MutableRetentionConfiguration newConfiguration ) {
            String functionName = newConfiguration.findAggregationNameThroughATonOfRegexes( metricName );
            cache.put( metricName, functionName );
            tracker.cacheMiss( cache.size(), cache.maxSize() );
            return log.exit(functionName);
        }
    }

	@Override
	public Optional<List<RetentionLevel>> findReadLevelsForMetric(String name) {
		log.entry(name);
		ArrayList<RetentionLevel> retentionLevelsList = new ArrayList<>(); 
        for ( Entry<Pattern, List<String>> entry : readLevelsMap.entrySet() ) {
            if ( entry.getKey().matcher( name ).find() ) {
            	for(String s : entry.getValue()){
            		if( levels.containsKey(s) ){
            			retentionLevelsList.add(levels.get(s));
            		}
            	}
                return log.exit(Optional.of(retentionLevelsList));
            }
        }
        log.warn("No ReadLevel found for {}", name);
        Optional<RetentionLevel> writeLevel = findWriteLevelForMetric(name);
        if(writeLevel.isPresent()){
        	return log.exit(Optional.of(Arrays.asList(writeLevel.get())));
        }
		return log.exit(Optional.empty());
	}

	@Override
	public Optional<RetentionLevel> findWriteLevelForMetric(String name) {
		log.entry(name);
        for ( Entry<Pattern, String> entry : writeLevelMap.entrySet() ) {
            if ( entry.getKey().matcher( name ).find() && levels.containsKey( entry.getValue() ) ) {
                return log.exit(Optional.of(levels.get( entry.getValue() )));
            }
        }
        log.warn("No WriteLevel found for {}", name);
		return log.exit(Optional.empty());
	}

	@Override
	public Collection<RetentionLevel> getAllLevels() {
		return levels.values();
	}

	@Override
	public OptionalLong getNameRetentionForName(String name) {
		log.entry(name);
        for ( Entry<Pattern, Long> entry : nameRetentionMap.entrySet() ) {
            if ( entry.getKey().matcher( name ).find() ) {
                return log.exit(OptionalLong.of(entry.getValue()));
            }
        }
        log.warn( "No Name Retention configured for {}", name );
		return log.exit(OptionalLong.empty());
	}

	@Override
	public List<RetentionLevel> getTopologicalSort(){
		if(topologicalSort != null){
			return topologicalSort;
		}
		log.trace( "Trying to create TopologicalSort on {} ", levels );
		topologicalSort = new LinkedList<>();
		Set<RetentionLevel> unvisited = new HashSet<>(levels.values());
		Stack<RetentionLevel> levelStack = new Stack<>();
		while(unvisited.size() > 0){
			levelStack.push(unvisited.iterator().next());
			while(!levelStack.isEmpty()){
				RetentionLevel level = levelStack.pop();
				if(levelStack.contains(level)){
					throw new IllegalArgumentException();
				}
				if(unvisited.contains(level)){
					levelStack.push(level);
					unvisited.remove(level);
					if(levels.containsKey(level.next())){
						levelStack.push(levels.get(level.next()));
						continue;
					}
					levelStack.pop();
				}
				if(!topologicalSort.contains(level)){
					topologicalSort.addFirst(level);
				}
			}
		}
		return log.exit(topologicalSort);
	}
}
