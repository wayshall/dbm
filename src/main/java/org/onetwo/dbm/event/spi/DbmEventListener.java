package org.onetwo.dbm.event.spi;

public interface DbmEventListener<S, E extends DbmEvent<S>> {
	
	public void doEvent(E event);

}
