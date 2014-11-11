package com.goodgame.profiling.rewrite_framework.statistics;

import java.time.Duration;
import java.time.Instant;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.kohsuke.MetaInfServices;

import com.goodgame.profiling.commons.statistics.WriteToStorageEvent;
import com.goodgame.profiling.commons.statistics.duration.PartitionedDurationStatistics;
import com.goodgame.profiling.commons.statistics.duration.PartitionedDurationStatisticsMBean;
import com.goodgame.profiling.commons.statistics.eventbus.EventBusManager;
import com.goodgame.profiling.commons.statistics.gathering.StatisticGatherer;
import com.goodgame.profiling.commons.statistics.jmx.MBeanManager;
import com.goodgame.profiling.commons.statistics.storage.MetricStorage;
import com.goodgame.profiling.commons.statistics.units.format.DurationFormatter;
import com.goodgame.profiling.commons.util.stopwatch.AsyncClock;
import com.goodgame.profiling.commons.util.stopwatch.Stopwatch;

@MetaInfServices
public class UpdateStatusReporter implements StatisticGatherer {
    private static final Logger log = LogManager.getLogger();

    private static final Marker UPDATED_STARTED = MarkerManager.getMarker("UPDATED_STARTED");

    private static final DurationFormatter formatter = new DurationFormatter();

    private AsyncClock clock = new AsyncClock();
    private Stopwatch totalTime = new Stopwatch( clock );
    private Stopwatch thisUpdateTime = new Stopwatch( clock );

    private Instant updateStarted;

    private boolean success;

    private final PartitionedDurationStatistics partitionedDurationStatistics = new PartitionedDurationStatistics(
            10, 10, 100);

    @Override
    public void init() {
        MBeanManager.registerStandardMBean(partitionedDurationStatistics, this
                .getClass().getPackage().getName() + ":type=" + this.getClass().getSimpleName(),
                PartitionedDurationStatisticsMBean.class);

        EventBusManager.subscribe(UpdateStartedEvent.class, e -> {
                        // Do NOT change this to Update! This typo makes people smile, but is otherwise unimportant.
                        log.info(UPDATED_STARTED, "/==================== Updated started ====================\\");
                        success = false;
                        updateStarted = e.when();
                        clock.setInstant( e.when() );
                        totalTime.start( );
                        thisUpdateTime.reset();
                        thisUpdateTime.start( );
        });
        EventBusManager.subscribe(UpdateFinishedEvent.class, e -> {
                        clock.setInstant( e.when() );
                        totalTime.stop();
                        thisUpdateTime.stop();
                        Duration updateTime = thisUpdateTime.duration();
                        success = e.success();
                        if (e.success()) {
                            log.info("\\==================== Update complete - took " + formatter.format(updateTime) + " ====================/");
                        } else {
                            log.warn("\\==================== Update failed - took " + formatter.format(updateTime) + " ====================/");
                        }

                        partitionedDurationStatistics.handleCall(updateStarted, "Updates", e.when());
        });
        EventBusManager.subscribe( WriteToStorageEvent.class, e -> {
            MetricStorage storage = e.storageToWriteTo();
            clock.setInstant( e.when() );
            MetricStorage subStorage = storage.getSubStorageCalled("update");
            subStorage.store("totalTimeUpdatingNanos", totalTime.duration().toNanos() );
            subStorage.store("thisUpdateTimeUpdatingNanos", thisUpdateTime.duration().toNanos() );
            subStorage.store("success", success ? 1 : 0);
        });
    }
}
