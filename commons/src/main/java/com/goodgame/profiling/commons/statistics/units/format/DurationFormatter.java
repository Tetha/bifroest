package com.goodgame.profiling.commons.statistics.units.format;

import java.time.Duration;

public class DurationFormatter {
    private TimeFormatter internalFormatter;

    public DurationFormatter() {
        this.internalFormatter = new TimeFormatter();
    }

    public String format( Duration duration ) {
        return internalFormatter.format( duration.toNanos() / 1e9d );
    }
}
