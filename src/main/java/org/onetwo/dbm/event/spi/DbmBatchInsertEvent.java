package org.onetwo.dbm.event.spi;

import org.onetwo.dbm.event.internal.DbmSessionEventSource;

public class DbmBatchInsertEvent extends DbmInsertEvent {
	private Integer batchSize;

	public DbmBatchInsertEvent(Object object, DbmSessionEventSource eventSource) {
		super(object, eventSource);
		setAction(DbmEventAction.batchInsert);
	}

	public Integer getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(Integer batchSize) {
		this.batchSize = batchSize;
	}

}
