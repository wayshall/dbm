package org.onetwo.dbm.event.internal;

import com.google.common.eventbus.EventBus;

/**
 * @author wayshall
 * <br/>
 */
public class EdgeEventBus {
	final private EventBus eventBus = new EventBus("dbm-edge-event-bus");
	
	public void register(Object listener){
		eventBus.register(listener);
	}
	
	public void post(Object event){
		this.eventBus.post(event);
	}

	final protected EventBus getEventBus() {
		return eventBus;
	}
	
	

}
