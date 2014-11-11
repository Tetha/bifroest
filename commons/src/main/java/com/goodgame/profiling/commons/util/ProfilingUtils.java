package com.goodgame.profiling.commons.util;

import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class, with general functions we seem to use often.
 */
public class ProfilingUtils {
    private ProfilingUtils() {
        // Utility class - avoid instantiation
    }

    /**
     * Get a string with the current time stamp in format
     * <code>yyyy-MM-dd HH:mm:ss,SSS</code>.
     */
    public static String getCurrentTimeStamp() {
        Clock clock = Clock.system( ZoneId.of( "Europe/Berlin" ) );
        ZonedDateTime now = ZonedDateTime.now( clock );
        return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format( now );
    }
}
