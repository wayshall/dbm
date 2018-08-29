package org.onetwo.dbm.event.spi;


public interface DbmEvent<S> {

	DbmEventAction getAction();

	S getEventSource();

}