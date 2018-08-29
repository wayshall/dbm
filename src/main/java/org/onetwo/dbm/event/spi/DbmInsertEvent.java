package org.onetwo.dbm.event.spi;

import org.onetwo.dbm.event.internal.DbmSessionEventSource;

public class DbmInsertEvent extends DbmSessionEvent{

	private boolean fetchId;
	
	public DbmInsertEvent(Object object, DbmSessionEventSource eventSource) {
		super(object, DbmEventAction.insert, eventSource);
	}

	public boolean isFetchId() {
		return fetchId;
	}

	public void setFetchId(boolean fetchId) {
		this.fetchId = fetchId;
	}

}
