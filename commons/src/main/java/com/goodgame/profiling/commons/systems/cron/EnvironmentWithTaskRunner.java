package com.goodgame.profiling.commons.systems.cron;

import com.goodgame.profiling.commons.boot.interfaces.Environment;

public interface EnvironmentWithTaskRunner extends Environment {

	TaskRunner taskRunner();

}
