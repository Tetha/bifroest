package com.goodgame.profiling.commons.systems.cron;

import java.util.concurrent.TimeUnit;

public class OneTimeTask extends Thread implements StoppableTask {
	private final Runnable task;
	private final long initialDelayInMillis;
	
	public OneTimeTask(Runnable task, String name, long initialDelay, TimeUnit unit) {
		super(name);
		this.task = task;
		this.initialDelayInMillis = TimeUnit.MILLISECONDS.convert(initialDelay, unit);
	}
	
	@Override
	public void run() {
		try {
			Thread.sleep(initialDelayInMillis);
		} catch (InterruptedException e) {
			// abandon task
			return;
		}
		task.run();
	}

	@Override
	public void stopYourself() {
		this.interrupt();
	}
}
