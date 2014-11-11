package com.goodgame.profiling.rewrite_framework.systems.gatherer.optimization;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OfficeHoursStrategy implements ShouldRunOptimizerStrategy {
    private static final Logger log = LogManager.getLogger();

    @Override
    public boolean shouldRun( ) {
        LocalDateTime date = LocalDateTime.now( ZoneId.of("Europe/Berlin") );
        int dow = date.getDayOfWeek().getValue();
        int hour = date.getHour();

        log.trace( "Day of week: " + dow );
        log.trace( "Hour: " + hour );

        return (1 <= dow) && (dow <= 4) && (9 <= hour) && (hour < 15);
    }
}
