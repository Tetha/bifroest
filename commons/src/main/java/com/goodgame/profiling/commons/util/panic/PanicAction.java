package com.goodgame.profiling.commons.util.panic;

import java.time.Duration;
import java.time.Instant;

public interface PanicAction {
    void execute( Instant now );
    Duration getCooldown();
}
