package com.goodgame.profiling.graphite_bifroest.systems.cassandra;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Cluster.Builder;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.TableMetadata;
import com.datastax.driver.core.querybuilder.Clause;
import com.datastax.driver.core.querybuilder.Ordering;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.goodgame.profiling.commons.model.Interval;
import com.goodgame.profiling.commons.model.Metric;
import com.goodgame.profiling.graphite_retentions.RetentionConfiguration;
import com.goodgame.profiling.graphite_retentions.RetentionTable;

public class CassandraDatabase {

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

    public CassandraDatabase( String user, String pass, String keyspace, String[] hosts, RetentionConfiguration retention ) {
        this.retention = retention;
        this.user = user;
        this.pass = pass;
        this.keyspace = keyspace;
        this.hosts = hosts;
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
                log.warn( "Table " + metadata.getName() + " doesn't match format." );
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

    public Long loadHighestTimestamp( RetentionTable table, String metricName ) {
        if ( session == null ) {
            open();
        }

        Statement stm = QueryBuilder
                .select( COL_TIME )
                .from( table.tableName() )
                .where( QueryBuilder.eq(COL_NAME, metricName) )
                .orderBy( QueryBuilder.desc(COL_TIME) )
                .limit( 1 );
        ResultSet results = session.execute(stm);
        Row row = results.one();
        if (row == null) {
            return null;
        } else {
            return row.getLong( COL_TIME );
        }
    }

    public Iterable<Metric> loadMetrics( RetentionTable table, String name, Interval interval ) {
        if ( session == null ) {
            open();
        }
        Clause cName = QueryBuilder.eq( COL_NAME, name );
        Ordering order = QueryBuilder.desc( COL_TIME );
        // start inclusive, end exclusive
        Clause cBtm = QueryBuilder.gte( COL_TIME, interval.start() );
        Clause cTop = QueryBuilder.lt( COL_TIME, interval.end() );
        Statement stm = QueryBuilder.select().all().from( table.tableName() ).where( cName ).and( cBtm ).and( cTop ).orderBy( order );
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

}
