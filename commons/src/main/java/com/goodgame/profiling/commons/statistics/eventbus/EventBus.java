package com.goodgame.profiling.commons.statistics.eventbus;

public interface EventBus {
	public void fire( final Object event );
	public void synchronousFire( final Object event );
	public < EVENT_CLASS > void subscribe( Class<EVENT_CLASS> event, EventBusSubscriber<EVENT_CLASS> subscriber );
	
	//FIXME: Remove
	public void shutdown() throws InterruptedException;
}
