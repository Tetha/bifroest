package com.goodgame.profiling.rewrite_framework.drain.persistent;

import java.io.IOException;
import java.util.List;

import com.goodgame.profiling.commons.model.Metric;
import com.goodgame.profiling.commons.util.panic.ProfilingPanic;
import com.goodgame.profiling.rewrite_framework.core.drain.AbstractBasicDrain;

public class PersistentDrainFrontend extends AbstractBasicDrain {

    private final PersistentDrain inner;

    public PersistentDrainFrontend( PersistentDrain inner ) {
        this.inner = inner;
    }

    @Override
    public void output( List<Metric> metrics ) throws IOException {
        try {
            inner.output( metrics );
        } catch ( RuntimeException | IOException e ) {
            ProfilingPanic.INSTANCE.panic();
            throw( e );
        }
    }
}
