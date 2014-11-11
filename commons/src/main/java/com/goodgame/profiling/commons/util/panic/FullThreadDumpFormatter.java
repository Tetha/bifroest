package com.goodgame.profiling.commons.util.panic;

import java.io.IOException;
import java.io.Writer;
import java.lang.Thread.State;
import java.lang.management.ThreadInfo;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class FullThreadDumpFormatter implements ThreadDumpFormatter {
    private final Writer writer;

    public FullThreadDumpFormatter( Writer writer ) {
        this.writer = writer;
    }

    @Override
    public void formatThreadDump( ThreadInfo[] threadInfos, Map<Thread, StackTraceElement[]> stacks ) throws IOException {
        Map<Long, ThreadInfo> threadInfoMap = new HashMap<Long, ThreadInfo>();
        for( ThreadInfo threadInfo : threadInfos ) {
            // threadInfos may contain null elements!
            if ( threadInfo == null ) {
                continue;
            }
            threadInfoMap.put( threadInfo.getThreadId(), threadInfo );
        }

        try {
            writer.write( "Dump of " + stacks.size() + " threads at " + ZonedDateTime.now( ZoneId.of( "Europe/Berlin" ) ).format( DateTimeFormatter.ISO_LOCAL_DATE_TIME ) + "\n" + "\n" );
            for( Map.Entry<Thread, StackTraceElement[]> entry : stacks.entrySet() ) {
                Thread thread = entry.getKey();
                writer.write( "\"" + thread.getName() + "\" prio=" + thread.getPriority() + " tid=" + thread.getId() + " " + thread.getState() + " " + ( thread.isDaemon() ? "deamon" : "worker" ) + "\n" );
                ThreadInfo threadInfo = threadInfoMap.get( thread.getId() );
                if ( threadInfo != null ) {
                    writer.write( "    native=" + threadInfo.isInNative() + ", suspended=" + threadInfo.isSuspended() + ", block=" + threadInfo.getBlockedCount() + ", wait=" + threadInfo.getWaitedCount() + "\n" );
                    writer.write( "    lock=" + threadInfo.getLockName() + " owned by " + threadInfo.getLockOwnerName() + " (" + threadInfo.getLockOwnerId() + ")\n" );
                }
                for( StackTraceElement element : entry.getValue() ) {
                    writer.write( "        " );
                    writer.write( element.toString() );
                    writer.write( "\n" );
                }
            }
            writer.write( "------------------------------------------------------" );
            writer.write( "\n" );
            writer.write( "Non-daemon threads: " );
            for( Thread thread : stacks.keySet() ) {
                if ( !thread.isDaemon() ) {
                    writer.write( "\"" + thread.getName() + "\", " );
                }
            }
            writer.write( "\n" );
            writer.write( "------------------------------------------------------" );
            writer.write( "\n" );
            writer.write( "Blocked threads: " );
            for( Thread thread : stacks.keySet() ) {
                if ( thread.getState() == State.BLOCKED ) {
                    writer.write( "\"" + thread.getName() + "\", " );
                }
            }
            writer.write( "\n" );
        } finally {
            writer.close();
        }
    }
}
