package com.goodgame.profiling.graphite_aggregator.systems.cassandra;

import com.goodgame.profiling.commons.boot.interfaces.Environment;

public interface EnvironmentWithCassandra extends Environment {

    CassandraAccessLayer cassandraAccessLayer();

}
