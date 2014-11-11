package com.goodgame.profiling.commons.systems.cron;

public interface EnvironmentWithMutableTaskRunner extends EnvironmentWithTaskRunner {

	void setTaskRunner( TaskRunner taskRunner );

}
