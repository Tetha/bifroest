package com.goodgame.profiling.rewrite_framework.systems;

import java.nio.file.Path;

import com.goodgame.profiling.commons.boot.InitD;
import com.goodgame.profiling.commons.boot.interfaces.Environment;
import com.goodgame.profiling.commons.systems.common.AbstractCommonEnvironment;
import com.goodgame.profiling.commons.systems.cron.EnvironmentWithMutableTaskRunner;
import com.goodgame.profiling.commons.systems.cron.TaskRunner;
import com.goodgame.profiling.graphite_retentions.RetentionConfiguration;
import com.goodgame.profiling.graphite_retentions.bootloader.EnvironmentWithMutableRetentionStrategy;
import com.goodgame.profiling.rewrite_framework.systems.persistent_drains.EnvironmentWithMutablePersistentDrainManager;
import com.goodgame.profiling.rewrite_framework.systems.persistent_drains.PersistentDrainManager;

public abstract class GathererEnvironment< I, U > extends AbstractCommonEnvironment
implements EnvironmentWithMutableTaskRunner, EnvironmentWithMutablePersistentDrainManager, EnvironmentWithMutableRetentionStrategy {

	private TaskRunner cron;
	private PersistentDrainManager<? extends Environment> persistentDrainManager;
	private RetentionConfiguration retentions;

	public GathererEnvironment( Path configPath, InitD init ) {
		super( configPath, init );
	}

	@Override
	public TaskRunner taskRunner() {
		return cron;
	}

	@Override
	public void setTaskRunner( TaskRunner taskRunner ) {
		this.cron = taskRunner;
	}

	@Override
	public PersistentDrainManager<? extends Environment> persistentDrainManager() {
		return persistentDrainManager;
	}

	@Override
	public void setPersistentDrainManager(PersistentDrainManager<? extends Environment> persistentDrainManager) {
		this.persistentDrainManager = persistentDrainManager;
	}
	
	@Override
	public RetentionConfiguration retentions() {
	    return retentions;
	}
	
	@Override
	public void setRetentions( RetentionConfiguration retentions ) {
	    this.retentions = retentions;
	}
}
