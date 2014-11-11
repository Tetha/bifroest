package com.goodgame.profiling.graphite_retentions.bootloader;

import com.goodgame.profiling.commons.boot.interfaces.Environment;
import com.goodgame.profiling.graphite_retentions.RetentionConfiguration;

public interface EnvironmentWithRetentionStrategy extends Environment {

    RetentionConfiguration retentions();

}
