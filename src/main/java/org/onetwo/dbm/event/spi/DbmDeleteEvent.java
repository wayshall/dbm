package org.onetwo.dbm.event.spi;

import org.onetwo.dbm.event.internal.DbmSessionEventSource;

public class DbmDeleteEvent extends DbmSessionEvent{
	
	private DeleteType deleteType = DeleteType.BY_IDENTIFY;//DeleteType.byIdentify;//
	public DbmDeleteEvent(Object object, DbmSessionEventSource eventSource) {
		super(object, DbmEventAction.delete, eventSource);
	}


	public boolean isDeleteByIdentify() {
		return DeleteType.BY_IDENTIFY==deleteType;
	}

	public boolean isDeleteAll() {
		return DeleteType.DELETE_ALL==deleteType;
	}

	public DeleteType getDeleteType() {
		return deleteType;
	}

	public void setDeleteType(DeleteType deleteType) {
		this.deleteType = deleteType;
	}

	public static enum DeleteType {
//		BY_ENTITY,
		BY_IDENTIFY,
		DELETE_ALL,
	}
}
