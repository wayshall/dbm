package org.onetwo.dbm.event.internal;


import java.util.ArrayList;
import java.util.List;

import org.onetwo.common.utils.Assert;
import org.onetwo.dbm.event.spi.DbmDeleteEvent;
import org.onetwo.dbm.event.spi.DbmSessionEvent;
import org.onetwo.dbm.exception.EntityNotFoundException;
import org.onetwo.dbm.exception.EntityVersionException;
import org.onetwo.dbm.mapping.DbmMappedEntry;
import org.onetwo.dbm.mapping.JdbcStatementContext;

public class DbmDeleteEventListener extends AbstractDbmEventListener {

	@Override
	public void doEvent(DbmSessionEvent event) {
		DbmDeleteEvent deleteEvent = (DbmDeleteEvent) event;
		Object entity = event.getObject();
		DbmSessionEventSource es = event.getEventSource();
		DbmMappedEntry entry = es.getMappedEntryManager().findEntry(entity!=null?entity:event.getEntityClass());
		if(entry==null)
			entry = es.getMappedEntryManager().findEntry(entity);
		Assert.notNull(entry, "can not find entry : " + event.getEntityClass()+"");
		
		this.doDelete(deleteEvent, entry);
	}
	
	public void doDelete(DbmDeleteEvent deleteEvent, DbmMappedEntry entry){
		Object entity = deleteEvent.getObject();
		DbmSessionEventSource es = deleteEvent.getEventSource();
		
		int count = 0;
		if(deleteEvent.isDeleteAll()){
			JdbcStatementContext<Object[]> delete = entry.makeDeleteAll();
//			count = es.getJFishJdbcTemplate().update(delete.getSql(), delete.getValue());
			List<Object[]> argList = new ArrayList<>(1);
			argList.add(delete.getValue());
			count = this.executeJdbcUpdate(delete.getSql(), argList, es);
		}else{
			checkEntityLastVersion(es, entry, entity);
			
			JdbcStatementContext<List<Object[]>> delete = entry.makeDelete(entity);
			List<List<Object[]>> argList = new ArrayList<>(1);
			argList.add(delete.getValue());
			count = this.executeJdbcUpdate(delete.getSql(), delete.getValue(), es);
			
			if(count<1){
				if(entry.isVersionControll()){
					throw new EntityVersionException(entry.getEntityClass(), entry.getId(entity), entry.getVersionValue(entity));
				}else{
					throw new EntityNotFoundException("update count is " + count + ".", entity.getClass(), entry.getId(entity));
				}
			}
		}
		/*if(count<1)
			throw new JFishOrmException("can not delete any entity["+entry.getEntityClass()+"] : " + count);*/
		deleteEvent.setUpdateCount(count);
	}

}
