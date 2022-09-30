package org.onetwo.dbm.event.spi;

import org.onetwo.dbm.event.internal.DbmSessionEventSource;

public class DbmInsertOrUpdateEvent extends DbmSessionEvent {

	private final boolean dynamicUpdate;
	

	public DbmInsertOrUpdateEvent(Object object, DbmSessionEventSource eventSource) {
		this(object, true, eventSource);
	}
	
	public DbmInsertOrUpdateEvent(Object object, boolean dynamicUpdate, DbmSessionEventSource eventSource) {
		super(object, DbmEventAction.insertOrUpdate, eventSource);
		this.dynamicUpdate = dynamicUpdate;
	}

	public boolean isDynamicUpdate() {
		return dynamicUpdate;
	}

}
