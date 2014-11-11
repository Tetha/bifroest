package com.goodgame.profiling.rewrite_framework.drain.persistent.cassandra;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections4.map.LazyMap;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.TableMetadata;
import com.datastax.driver.core.exceptions.AlreadyExistsException;
import com.datastax.driver.core.exceptions.WriteTimeoutException;
import com.datastax.driver.core.policies.ConstantReconnectionPolicy;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.goodgame.profiling.commons.model.Metric;
import com.goodgame.profiling.commons.statistics.eventbus.EventBusManager;
import com.goodgame.profiling.graphite_retentions.RetentionConfiguration;
import com.goodgame.profiling.graphite_retentions.RetentionTable;
import com.goodgame.profiling.rewrite_framework.drain.persistent.PersistentDrain;
import com.goodgame.profiling.rewrite_framework.drain.statistics.DrainMetricOutputEvent;

public class PersistentCassandraDrain implements PersistentDrain {

    private static final Logger log = LogManager.getLogger();

    private static final String COL_NAME = "metric";
    private static final String COL_TIME = "timestamp";
    private static final String COL_VALUE = "value";
    private static final String[] COLUMNS = { COL_NAME, COL_TIME, COL_VALUE };

    private final RetentionConfiguration retentions;
    private final String keyspace;
    private final Cluster cluster;
    private final Session session;

    public PersistentCassandraDrain( String username, String password, String[] seeds, String keyspace, RetentionConfiguration retentions ) {
        Cluster.Builder builder = Cluster.builder().addContactPoints( seeds );
        builder.withReconnectionPolicy( new ConstantReconnectionPolicy( 500 ) );
        if ( username != null ) {
            if ( password != null ) {
                builder = builder.withCredentials( username, password );
            } else {
                log.warn( "username was set, password was NOT set - IGNORING username!" );
            }
        }

        this.cluster = builder.build();
        this.keyspace = keyspace;
        this.session = cluster.connect( keyspace );
        this.retentions = retentions;
    }

    @Override
    public void shutdown() {
        session.close();
        cluster.close();
    }

    @Override
    public void output( Collection<Metric> metrics ) {
        if( metrics.size() == 0 ) {
            return;
        }

        Map<RetentionTable, BatchStatement> stms = LazyMap.<RetentionTable, BatchStatement>lazyMap( new HashMap<>(), () -> new BatchStatement() );
        for ( Metric metric : metrics ) {
            insertMetricIntoBatch( metric, stms );
        }
        KeyspaceMetadata metadata = cluster.getMetadata().getKeyspace( keyspace );
        for (RetentionTable table : stms.keySet()) {
            createTableIfNecessary( table, metadata );
        }
        for ( BatchStatement batch : stms.values() ) {
            try {
                session.execute( batch );
            } catch ( WriteTimeoutException e ) {
                log.info( "WriteTimeoutException while sending Metrics to cassandra." );
                log.info( e.getMessage() );
                log.info( "According to http://www.datastax.com/dev/blog/how-cassandra-deals-with-replica-failure, this is harmless" );
            }
        }
        EventBusManager.fire( new DrainMetricOutputEvent( ( new PersistentCassandraDrainFactory<>().handledType() ), metrics.size() ) );
    }

    private void insertMetricIntoBatch( Metric metric, Map<RetentionTable, BatchStatement> map ) {
        Object[] values = { metric.name(), metric.timestamp(), metric.value() };
        Optional<RetentionTable> table = retentions.findWriteTableForMetric( metric );
        if ( !table.isPresent() ) {
            log.warn( "No retention defined - not outputting metric! {}", metric );
        } else {
            map.get( table.get() ).add( QueryBuilder.insertInto( table.get().tableName() ).values( COLUMNS, values ) );
        }
    }
    
    private void createTableIfNecessary( RetentionTable table, KeyspaceMetadata metadata ) {
        for ( TableMetadata meta : metadata.getTables()) {
            log.debug( "Comparing " + meta.getName() + " with " + table.tableName() );
            if ( meta.getName().equalsIgnoreCase( table.tableName() )) {
                return;
            }
        }
         
        StringBuilder query = new StringBuilder();
        query.append( "CREATE TABLE " ).append( table.tableName() ).append( " (" );
        query.append( COL_NAME ).append( " text, " );
        query.append( COL_TIME ).append( " bigint, " );
        query.append( COL_VALUE ).append( " double, " );
        query.append( "PRIMARY KEY (" ).append( COL_NAME ).append( ", " ).append( COL_TIME ).append( ")");
        query.append( ");" );
        log.debug( "Creating table with query: <" + query.toString() + ">");
        try {
            session.execute( query.toString() );
        } catch( AlreadyExistsException e ) {
            // Some other gatherer might have already created the same table.
        }
    }

    public void dumpInfos() {
        log.info( ReflectionToStringBuilder.toString( cluster.getMetadata() ) );
    }
}
