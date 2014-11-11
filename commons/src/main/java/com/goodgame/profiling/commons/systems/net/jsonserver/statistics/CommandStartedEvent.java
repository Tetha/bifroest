package com.goodgame.profiling.commons.systems.net.jsonserver.statistics;

import java.time.Clock;
import java.util.Objects;

import com.goodgame.profiling.commons.statistics.commands.EventWithThreadId;
import com.goodgame.profiling.commons.statistics.process.ProcessStartedEvent;

public class CommandStartedEvent extends ProcessStartedEvent implements EventWithThreadId {
    private final String command;
    private final String interfaceName;
    private final long threadId;

    public CommandStartedEvent( Clock clock, String command, String interfaceName, long threadId ) {
        super( clock );
        this.command = Objects.requireNonNull( command );
        this.interfaceName = Objects.requireNonNull( interfaceName );
        this.threadId = Objects.requireNonNull( threadId );
    }

    public String command() {
        return command;
    }

    public String interfaceName() {
        return interfaceName;
    }

    @Override
    public long threadId() {
        return threadId;
    }

    @Override
    public String toString() {
        return String.format( "CommandStartedEvent[ command=%s, threadId=%d, when=%s ]", command, threadId, when() );
    }
}
