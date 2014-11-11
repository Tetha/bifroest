package com.goodgame.profiling.commons.systems.statistics.push_strategy.with_task;

import java.io.IOException;

import com.goodgame.profiling.commons.systems.cron.EnvironmentWithTaskRunner;
import com.goodgame.profiling.commons.systems.cron.TaskRunner.TaskID;
import com.goodgame.profiling.commons.systems.statistics.push_strategy.StatisticsPushStrategy;

public abstract class StatisticsPushStrategyWithTask<E extends EnvironmentWithTaskRunner> implements StatisticsPushStrategy {
    private final E environment;

    private TaskID taskId;

    public StatisticsPushStrategyWithTask( E environment ) {
        this.environment = environment;
    }

    public void setTaskId( TaskID taskId ) {
        this.taskId = taskId;
    }

    @Override
    public void close() throws IOException {
        environment.taskRunner().stopTask( taskId );
        closeAfterTaskStopped();
    }

    public abstract void closeAfterTaskStopped() throws IOException;
}
