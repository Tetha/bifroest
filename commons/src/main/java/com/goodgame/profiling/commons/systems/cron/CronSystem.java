package com.goodgame.profiling.commons.systems.cron;

import java.util.Arrays;
import java.util.Collection;

import com.goodgame.profiling.commons.boot.interfaces.Subsystem;
import com.goodgame.profiling.commons.systems.SystemIdentifiers;

public class CronSystem< E extends EnvironmentWithMutableTaskRunner > implements Subsystem<E> {

	@Override
	public String getSystemIdentifier() {
		return SystemIdentifiers.CRON;
	}

	@Override
	public Collection<String> getRequiredSystems() {
		return Arrays.asList( SystemIdentifiers.LOGGING );
	}

	@Override
	public void boot( E environment ) throws Exception {
		environment.setTaskRunner( new TaskRunner() );
	}

	@Override
	public void shutdown( E environment ) {
		environment.taskRunner().shutdown();
	}
}
