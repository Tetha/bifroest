package com.goodgame.profiling.commons.systems.cron.observer;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.goodgame.profiling.commons.statistics.eventbus.EventBusManager;

public class WatchdogRepeatingTaskObserver implements RepeatingTaskObserver {
	private static final Logger log = LogManager.getLogger();
	
	private final String threadName;
	private final DelayQueue<StartStop> queue;
	private final long frequencyInMillis;
	private final long leewayInMillis;
	private final QueueChecker watchdog;
	
	public WatchdogRepeatingTaskObserver(String threadName, long frequencyInMillis) {
		this(threadName, frequencyInMillis, 0);
	}
	
	public WatchdogRepeatingTaskObserver(String threadName, long frequencyInMillis, long leewayInMillis) {
		this(threadName, frequencyInMillis, leewayInMillis, true);
	}
	
	// for testing
	WatchdogRepeatingTaskObserver(String threadName, long frequencyInMillis, long leewayInMillis, boolean daemonize) {
		this.threadName = threadName;
		this.frequencyInMillis = frequencyInMillis;
		this.leewayInMillis = leewayInMillis;
		this.queue = new DelayQueue<>();
		this.watchdog = new QueueChecker( threadName );
		this.watchdog.setDaemon(daemonize);
		this.watchdog.start();
	}

	@Override
	public void threadStarted(long startTimestampInMillis) {
		queue.put(new StartStop(startTimestampInMillis, '('));
		queue.put(new StartStop(startTimestampInMillis + frequencyInMillis + leewayInMillis, '|'));
	}

	@Override
	public void threadStopped(long stopTimestampInMillis) {
		queue.put(new StartStop(stopTimestampInMillis, ')'));
	}
	
	// for testing
	void shutdown(long shutdownTimestampInMillis) {
		log.trace("Putting .");
		
		queue.put(new StartStop(shutdownTimestampInMillis, '.'));
		try {
			watchdog.join();
		} catch (InterruptedException e) {
			// ignore
		}
	}
	
	private class QueueChecker extends Thread {
	    public QueueChecker( String threadName ) {
	        super( threadName );
	    }

		private int stoppedSeen = 0;
		private int shouldBeStoppedSeen = 0;
		
		@Override
		public void run() {
			log.debug("QueueChecker " + threadName + " online.");
			while (true) {
				try {
					char c = queue.take().parens;
					log.trace("Taking " + c);
					
					switch(c) {
					case '(':
						break;
					case ')':
						stoppedSeen++;
						break;
					case '|':
						shouldBeStoppedSeen++;
						if (shouldBeStoppedSeen != stoppedSeen) {
							EventBusManager.fire( new PreviousExecutionStillRunningEvent( threadName ) );
						}
						break;
					case '.':
						return;
					default:
						log.warn("Somethings wrong with the QueueChecker of " + threadName);
					}
				} catch (InterruptedException e) {
					log.warn("QueueChecker interrupted", e);
				}
			}
		}
	}

	private class StartStop implements Delayed {
		// '(' : Thread start
		// ')' : Thread stop
		// '|' : Thread stop should be until this point in time
		// '.' : Shutdown QueueChecker
		public final char parens;
		
		private final long targetTimeMillis;
		
		public StartStop(long targetTimeMillis, char parens) {
			this.parens = parens;
			this.targetTimeMillis = targetTimeMillis;
		}

		@Override
		public int compareTo(Delayed o) {
			return (int) Math.signum( this.getDelay( TimeUnit.MILLISECONDS ) - o.getDelay( TimeUnit.MILLISECONDS ) );
		}

		@Override
		public long getDelay(TimeUnit unit) {
			return unit.convert( targetTimeMillis - System.currentTimeMillis(), TimeUnit.MILLISECONDS );
		}
	}
}
