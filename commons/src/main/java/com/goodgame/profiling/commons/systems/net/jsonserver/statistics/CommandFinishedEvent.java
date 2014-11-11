package com.goodgame.profiling.commons.systems.net.jsonserver.statistics;

import java.time.Clock;

import com.goodgame.profiling.commons.statistics.commands.EventWithThreadId;
import com.goodgame.profiling.commons.statistics.process.ProcessFinishedEvent;

public class CommandFinishedEvent extends ProcessFinishedEvent implements EventWithThreadId {
    private final String command;
    private final String interfaceName;
    private final long threadId;

    public CommandFinishedEvent( Clock clock, String command, String interfaceName, long threadId, boolean success ) {
        super( clock, success );
        this.command = command;
        this.interfaceName = interfaceName;
        this.threadId = threadId;
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
        return String.format( "CommandFinishedEvent[ command=%s, threadId=%d, when=%s success=%s ]", command, threadId, when(), success() );
    }
}
