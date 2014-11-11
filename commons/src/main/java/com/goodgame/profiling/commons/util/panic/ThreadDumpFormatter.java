package com.goodgame.profiling.commons.util.panic;

import java.io.IOException;
import java.lang.management.ThreadInfo;
import java.util.Map;

public interface ThreadDumpFormatter {
    public void formatThreadDump( ThreadInfo[] threadInfos, Map<Thread, StackTraceElement[]> dump ) throws IOException;
}
