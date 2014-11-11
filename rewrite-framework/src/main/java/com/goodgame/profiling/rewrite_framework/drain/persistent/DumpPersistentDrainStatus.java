package com.goodgame.profiling.rewrite_framework.drain.persistent;

import java.time.Duration;
import java.time.Instant;

import com.goodgame.profiling.commons.util.panic.PanicAction;
import com.goodgame.profiling.rewrite_framework.systems.persistent_drains.EnvironmentWithPersistentDrainManager;

public class DumpPersistentDrainStatus<E extends EnvironmentWithPersistentDrainManager> implements PanicAction {
    private E env;

    public DumpPersistentDrainStatus( E env ) {
        this.env = env;
    }

    @Override
    public void execute( Instant now ) {
        for( PersistentDrain drain : env.persistentDrainManager().getAllPersistentDrains().values() ) {
            drain.dumpInfos();
        }
    }

    @Override
    public Duration getCooldown() {
        return Duration.ofMinutes( 1 );
    }
}
