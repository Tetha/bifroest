package com.goodgame.profiling.commons.systems.cron.observer;

public class DummyRepeatingTaskObserver implements RepeatingTaskObserver {
	@Override
	public void threadStarted(long start) {
		// empty
	}

	@Override
	public void threadStopped(long start) {
		// empty
	}
}
