package com.goodgame.profiling.commons.systems.cron;

public interface StoppableTask {
	void stopYourself();
	void join() throws InterruptedException;
}
