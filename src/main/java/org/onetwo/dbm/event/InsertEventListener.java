package org.onetwo.dbm.event;

import org.onetwo.dbm.mapping.DbmMappedEntry;

abstract public class InsertEventListener extends AbstractDbmEventListener {
	
	@Override
	public void doEvent(DbmSessionEvent event) {
		this.onInsert((DbmInsertEvent)event);
	}

	public void onInsert(DbmInsertEvent event) {
		Object entity = event.getObject();
		DbmSessionEventSource es = event.getEventSource();
		DbmMappedEntry entry = es.getMappedEntryManager().getEntry(entity);
//		event.setJoined(entry.isJoined());
		
		/*if(entry.isJoined()){
			Object findEntity = es.findById(entry.getEntityClass(), (Serializable)entity);
			if(findEntity!=null){
				this.logger.debug("joined entity["+LangUtils.toString(entity)+"] exist, ignore insert.");
				return ;
			}
		}*/
		
		this.executeDbmEntityListener(true, event, entity, entry.getEntityListeners());
		this.doInsert(event, entry);
		this.executeDbmEntityListener(false, event, entity, entry.getEntityListeners());
	}
	
	abstract protected void doInsert(DbmInsertEvent event, DbmMappedEntry entry);

	
	/*protected int invokeInsert(JFishInsertEvent event, String sql, List<Object[]> args, JFishEventSource es){
		return executeJdbcUpdate(event, sql, args, es);
	}*/
	
}
