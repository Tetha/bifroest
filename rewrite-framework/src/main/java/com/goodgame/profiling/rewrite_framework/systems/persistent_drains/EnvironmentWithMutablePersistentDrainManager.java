package com.goodgame.profiling.rewrite_framework.systems.persistent_drains;

import com.goodgame.profiling.commons.boot.interfaces.Environment;


public interface EnvironmentWithMutablePersistentDrainManager extends
		EnvironmentWithPersistentDrainManager {
	public void setPersistentDrainManager(PersistentDrainManager<? extends Environment> persistentDrainManager);
}
