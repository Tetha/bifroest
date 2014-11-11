package com.goodgame.profiling.graphite_aggregator.systems.cassandra;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Cluster.Builder;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.TableMetadata;
import com.datastax.driver.core.querybuilder.Clause;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.goodgame.profiling.commons.model.Metric;
import com.goodgame.profiling.commons.statistics.eventbus.EventBusManager;
import com.goodgame.profiling.graphite_aggregator.systems.cassandra.statistics.CreateTableEvent;
import com.goodgame.profiling.graphite_aggregator.systems.cassandra.statistics.DropTableEvent;
import com.goodgame.profiling.graphite_retentions.RetentionConfiguration;
import com.goodgame.profiling.graphite_retentions.RetentionTable;

public class CassandraAccessLayer {

    private static final Logger log = LogManager.getLogger();

    private static final String COL_NAME = "metric";
    private static final String COL_TIME = "timestamp";
    private static final String COL_VALUE = "value";

    private final String user;
    private final String pass;
    private final String keyspace;
    private final String[] hosts;

    private final RetentionConfiguration retention;
    private Cluster cluster;
    private Session session = null;

    private final boolean dryRun;

    public CassandraAccessLayer( String user, String pass, String keyspace, String[] hosts, RetentionConfiguration retention, boolean dryRun ) {
        this.retention = retention;
        this.user = user;
        this.pass = pass;
        this.keyspace = keyspace;
        this.hosts = hosts;
        this.dryRun = dryRun;

        if ( dryRun ) {
            log.warn( "Running with dryRun, NOT ACTUALLY DOING ANYTHING!!!" );
        }
    }

    public void open() {
        if ( cluster == null || session == null ) {
            Builder builder = Cluster.builder();
            builder.addContactPoints( hosts );
            if ( user != null && pass != null && !user.isEmpty() && !pass.isEmpty() ) {
                builder = builder.withCredentials( user, pass );
            }
            cluster = builder.build();
            session = cluster.connect( keyspace );
        }
    }

    public void close() {
        if ( session != null ) {
            session.close();
            session = null;
        }
        if ( cluster != null ) {
            cluster.close();
            cluster = null;
        }
    }

    public Iterable<RetentionTable> loadTables() {
        List<RetentionTable> ret = new ArrayList<>();

        Collection<TableMetadata> metadatas = cluster.getMetadata().getKeyspace( keyspace ).getTables();

        for ( TableMetadata metadata : metadatas ) {
            if ( RetentionTable.TABLE_REGEX.matcher( metadata.getName() ).matches() ) {
                ret.add( new RetentionTable( metadata.getName(), retention ) );
            } else {
                log.warn( "Table " + metadata.getName() + "doesn't match format." );
            }
        }

        return ret;
    }

    public Iterable<String> loadMetricNames( RetentionTable table ) {
        if ( session == null ) {
            open();
        }
        Statement stm = QueryBuilder.select().distinct().column( COL_NAME ).from( table.tableName() );
        final Iterator<Row> iter = session.execute( stm ).iterator();
        return new Iterable<String>() {

            @Override
            public Iterator<String> iterator() {
                return new Iterator<String>() {

                    @Override
                    public boolean hasNext() {
                        return iter.hasNext();
                    }

                    @Override
                    public String next() {
                        Row row = iter.next();
                        return row.getString( COL_NAME );
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    public Iterable<Metric> loadUnorderedMetrics( RetentionTable table, String name ) {
        if ( session == null ) {
            open();
        }
        Clause cName = QueryBuilder.eq( COL_NAME, name );
        Statement stm = QueryBuilder.select().all().from( table.tableName() ).where( cName );
        final Iterator<Row> iter = session.execute( stm ).iterator();
        return new Iterable<Metric>() {

            @Override
            public Iterator<Metric> iterator() {
                return new Iterator<Metric>() {

                    @Override
                    public boolean hasNext() {
                        return iter.hasNext();
                    }

                    @Override
                    public Metric next() {
                        Row row = iter.next();
                        return new Metric( row.getString( COL_NAME ), row.getLong( COL_TIME ), row.getDouble( COL_VALUE ) );
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    public void insertMetrics( RetentionTable table, Collection<Metric> metrics ) {
        if ( dryRun ) {
            log.debug( "Inserting " + metrics.toString() + " into " + table );
            return;
        }

        if ( session == null ) {
            open();
        }

        BatchStatement batch = new BatchStatement();
        for ( Metric metric : metrics ) {
            String[] columns = { COL_NAME, COL_TIME, COL_VALUE };
            Object[] values = { metric.name(), metric.timestamp(), metric.value() };
            Statement stm = QueryBuilder.insertInto( table.tableName() ).values( columns, values );
            batch.add( stm );
        }
        session.execute( batch );
    }

    public void createTableIfNecessary( RetentionTable table ) {
        if ( session == null ) {
            open();
        }
        Collection<TableMetadata> tables = cluster.getMetadata().getKeyspace( keyspace ).getTables();
        for ( TableMetadata meta : tables ) {
            if ( meta.getName().equalsIgnoreCase( table.tableName() ) ) {
                return;
            }
        }

        if ( dryRun ) {
            log.debug( "Creating table " + table );
            return;
        }

        StringBuilder query = new StringBuilder();
        query.append( "CREATE TABLE IF NOT EXISTS " ).append( table.tableName() ).append( " (" );
        query.append( COL_NAME ).append( " text, " );
        query.append( COL_TIME ).append( " bigint, " );
        query.append( COL_VALUE ).append( " double, " );
        query.append( "PRIMARY KEY (" ).append( COL_NAME ).append( ", " ).append( COL_TIME ).append( ")" );
        query.append( ");" );
        session.execute( query.toString() );
        EventBusManager.fire( new CreateTableEvent( System.currentTimeMillis(), table ) );
    }

    public void dropTable( RetentionTable table ) {
        if ( dryRun ) {
            log.debug( "Dropping " + table );
            return;
        }

        if ( session == null ) {
            open();
        }
        session.execute( "DROP TABLE " + table.tableName() + ";" );
        EventBusManager.fire( new DropTableEvent( System.currentTimeMillis(), table ) );
    }

}
