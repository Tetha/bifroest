package com.goodgame.profiling.rewrite_framework.core.drain;

import java.io.IOException;

public abstract class AbstractBasicDrain implements Drain {
    @Override
    public void close() throws IOException {
        // Do nothing
    }

    @Override
    public void flushRemainingBuffers() throws IOException {
        // Do nothing

    }
}
