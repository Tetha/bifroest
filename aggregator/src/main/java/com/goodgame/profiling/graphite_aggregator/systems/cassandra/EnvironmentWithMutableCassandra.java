package com.goodgame.profiling.graphite_aggregator.systems.cassandra;

public interface EnvironmentWithMutableCassandra extends EnvironmentWithCassandra {

    void setCassandraAccessLayer( CassandraAccessLayer cassandra );

}
