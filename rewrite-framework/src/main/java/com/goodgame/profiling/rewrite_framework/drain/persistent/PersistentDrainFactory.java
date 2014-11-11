package com.goodgame.profiling.rewrite_framework.drain.persistent;

import com.goodgame.profiling.commons.boot.interfaces.Environment;
import com.goodgame.profiling.rewrite_framework.core.BasicFactory;

public interface PersistentDrainFactory<E extends Environment, T extends PersistentDrain>
// BasicFactory, not BasicDrainFactory! We don't want these to get picked up by the "normal" serviceloader calls!
extends BasicFactory<E,T> {
}
