package com.goodgame.profiling.graphite_bifroest.systems.cassandra.wrapper;

import java.util.Iterator;

import com.goodgame.profiling.commons.model.Interval;
import com.goodgame.profiling.commons.model.Metric;
import com.goodgame.profiling.graphite_retentions.RetentionLevel;

class LeveledMetricIterator extends MetricIterator {

    private final Interval interval;
    private final RetentionLevel level;
    private final BlockIterator blocks;
    private final MetricIterator nextLevel;

    private Iterator<Metric> metrics;
    private Metric gapMetric;
    private long lastTimestamp;

    public LeveledMetricIterator( Interval interval, RetentionLevel level, BlockIterator blocks, MetricIterator nextLevel ) {
        this.nextLevel = nextLevel;
        this.interval = interval;
        this.level = level;
        this.blocks = blocks;
        this.gapMetric = null;
        this.lastTimestamp = interval.end();
        this.nextMetric = seekNext();
    }

    @Override
    protected Metric seekNext() {
        Metric metric = null;

        // Check if we are in a gap
        if ( gapMetric != null ) {
            metric = nextLevel.peekNext();
            if ( metric != null && metric.timestamp() <= gapMetric.timestamp() ) {
                metric = gapMetric;
                lastTimestamp = metric.timestamp();
                gapMetric = null;
            } else {
                metric = nextLevel.nextFrom( lastTimestamp );
                if ( metric == null || metric.timestamp() <= gapMetric.timestamp() ) {
                    metric = gapMetric;
                    lastTimestamp = metric.timestamp();
                    gapMetric = null;
                }
            }
            return metric;
        }

        // Skip anything outside the interval
        while ( metrics != null && metrics.hasNext() ) {
            metric = metrics.next();
            if ( interval.contains( metric.timestamp() ) ) {
                break;
            }
        }

        // Nothing found - load new block, or default to next level
        if ( metric == null ) {
            if ( blocks.hasNext() ) {
                metrics = blocks.next().iterator();
                return seekNext();
            } else {
                return nextLevel.nextFrom( lastTimestamp );
            }
        }

        // Check for gaps
        if ( metric.timestamp() < lastTimestamp - level.frequency() ) {
            gapMetric = metric;
            return seekNext();
        }

        // Everything is fine
        lastTimestamp = metric.timestamp();
        return metric;
    }

}
