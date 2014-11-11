package com.goodgame.profiling.commons.systems.common;

import java.nio.file.Path;

import com.goodgame.profiling.commons.boot.interfaces.Environment;

public interface EnvironmentWithConfigPath extends Environment {

	Path getConfigPath();

}
