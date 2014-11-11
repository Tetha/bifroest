package com.goodgame.profiling.commons.util.panic;

import java.time.Duration;
import java.time.Instant;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PanicAnnouncement implements PanicAction {
    private static final Logger log = LogManager.getLogger();

    @Override
    public void execute( Instant now ) {
        log.warn( "ServerPanic Triggered" );
    }

    @Override
    public Duration getCooldown() {
        return Duration.ofMinutes( 1 );
    }
}
