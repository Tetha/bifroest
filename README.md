# Bifroest

## Architecture
The project consists of 6 parts, profiling-commons, bifroest-retentinos,
bifroest-rewrite-framework, the stream-rewriter, the aggregator and 
bifroest itself.

The profiling-commons contain a number of shared code to create a 
long-running service on a server, such as logging configuration,
performance statistic collection, command interfaces and so on.

The retention-system implements shared code between the aggregator.
For one, the library implements value classes to hold the retention
strategies. Second, the retention system implements the aggregation
function. For example, if we have datapoints at one datapoint per
minute and need datapoints at one datapoint per 15 minutes, the
retention system provides a function to combine values into the
correct buckets.

The rewrite framework is required for the stream rewriter. It provides
a common base for so-called rewriters. Rewriters accept data through 
some sources, rewrites them into graphite metrics and outputs
the metrics to some destination. At GoodGame Studios, we have several
clients for the rewrite framework, but I can't hand them out just yet.

The stream rewriter itself implements the graphite tcp frontend. It's 
a quite simple application and a devent starting point into the code 
base.

The aggregator is our database janitor. It periodically removes 
and aggregates old data as necessary - or it deletes everything if
it feels like it. But - since the aggregator is quality software,
this never occured during the development of this software.

Bifroest, finally, is a cache for cassandra. It contains a prefix 
to answer Graphite's metric-find queries, such as "get me all
metrics matching "foo.\*.bar42.\*". It also contains the metric
cache, which is an assortment of ring buffers. The ring buffers
contain the most recent data for the metrics in the cache and they
are kept up-to-date bu the stream-rewriter on-the-fly. This makes
a lot of database hits unnecessary and maintains a short response 
time for the cache. 

## Build instructions
Unless I forgot more dependencies, it should be possible to start
up the three services in the following steps:

 - install the profiling-commons via mvn install
 - install the bifroest-retentions via mvn install
 - install the bifroest-rewrite-framework via mvn install

After these three steps, all three applications should start using
"mvn package exec:java", using configuration from ./test/config. Logs
are writen to ./logs and state is written to ./test/data.

## State of the snapshot
This is a snapshot of what we are cuurrently deploying. We have one
or two reworks in progress, but those are in a highly experimental
state and I'd like them to stabilize. The development of these 
features will be migrated to apache repositories if necessary.

Furthermore, I'm currently in the process of collecting our 
documentation into markdown files in the snapshot. 
