package com.goodgame.profiling.graphite_retentions.bootloader;

import com.goodgame.profiling.graphite_retentions.RetentionConfiguration;

public interface EnvironmentWithMutableRetentionStrategy extends EnvironmentWithRetentionStrategy {

    void setRetentions( RetentionConfiguration retentions );

}
