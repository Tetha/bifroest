package com.goodgame.profiling.commons.systems.cron.observer;

public interface RepeatingTaskObserver {
	void threadStarted(long startTimestampInMillis);
	void threadStopped(long startTimestampInMillis);
}
