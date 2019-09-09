package org.onetwo.dbm.event.internal;

import java.util.List;

import org.onetwo.common.utils.LangUtils;
import org.onetwo.dbm.event.spi.DbmSessionEvent;
import org.onetwo.dbm.event.spi.DbmUpdateEvent;
import org.onetwo.dbm.exception.DbmException;
import org.onetwo.dbm.mapping.DbmMappedEntry;
import org.onetwo.dbm.mapping.JdbcStatementContext;

public class DbmBatchUpdateEventListener extends UpdateEventListener {

	@Override
	public void doEvent(DbmSessionEvent event) {
		DbmMappedEntry entry = event.getEventSource().getMappedEntryManager().findEntry(event.getObject());
		if(entry==null){
			event.setUpdateCount(0);
			return ;
		}
		super.doEvent(event);
	}
	
	@Override
	protected void doUpdate(DbmUpdateEvent event, DbmMappedEntry entry){
		Object entity = event.getObject();
		if(!LangUtils.isMultiple(entity)){
			throw new DbmException("batch update's args must be a Collection or Array!");
		}
//		int count = this.executeJdbcUpdate(event.getEventSource(), entry.makeUpdate(entity));
		JdbcStatementContext<List<Object[]>> updates = entry.makeUpdate(entity);
		int count = this.executeJdbcUpdate(true, updates.getSql(), updates.getValue(), event.getEventSource());
		event.setUpdateCount(count);
	}

}
