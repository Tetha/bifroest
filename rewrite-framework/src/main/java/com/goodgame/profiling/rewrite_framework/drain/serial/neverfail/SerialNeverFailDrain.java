package com.goodgame.profiling.rewrite_framework.drain.serial.neverfail;

import java.util.Collection;
import java.util.List;

import com.goodgame.profiling.commons.model.Metric;
import com.goodgame.profiling.rewrite_framework.core.drain.Drain;
import com.goodgame.profiling.rewrite_framework.drain.serial.AbstractSerialDrain;

public final class SerialNeverFailDrain extends AbstractSerialDrain {
    public SerialNeverFailDrain( Collection<Drain> subs ) {
        super( subs );
    }

    @Override
    public void output( List<Metric> metrics ) {
        forEachDrainLoggingExceptions( s -> s.output( metrics ) );
    }
}
