package com.goodgame.profiling.rewrite_framework.systems.persistent_drains;

import com.goodgame.profiling.commons.boot.interfaces.Environment;

public interface EnvironmentWithPersistentDrainManager extends Environment {
	public PersistentDrainManager<? extends Environment> persistentDrainManager();
}
