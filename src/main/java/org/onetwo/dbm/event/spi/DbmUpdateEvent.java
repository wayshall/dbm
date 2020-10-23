package org.onetwo.dbm.event.spi;

import org.onetwo.dbm.event.internal.DbmSessionEventSource;

public class DbmUpdateEvent extends DbmSessionEvent{

	private boolean dynamicUpdate;
//	private boolean batchUpdate;
	private Integer batchSize;
	
	public DbmUpdateEvent(Object object, DbmSessionEventSource eventSource) {
		super(object, DbmEventAction.update, eventSource);
	}
	
	public boolean isDynamicUpdate() {
		return dynamicUpdate;
	}

	public void setDynamicUpdate(boolean dynamicUpdate) {
		this.dynamicUpdate = dynamicUpdate;
	}

	public Integer getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(Integer batchSize) {
		this.batchSize = batchSize;
	}

}
