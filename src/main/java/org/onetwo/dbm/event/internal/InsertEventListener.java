package org.onetwo.dbm.event.internal;

import org.onetwo.dbm.event.spi.DbmInsertEvent;
import org.onetwo.dbm.event.spi.DbmSessionEvent;
import org.onetwo.dbm.id.IdentifierGenerator;
import org.onetwo.dbm.mapping.DbmMappedEntry;
import org.onetwo.dbm.mapping.DbmMappedField;

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
	
	protected void setIdIfNecessary(DbmInsertEvent event, DbmMappedEntry entry, Object entity) {
//		if(entry.isEntity() && entry.getIdentifyField().isGeneratedValue()){
//			Serializable id = generatedIdentifyBeforeInsert(event, entry);
//			entry.setId(entity, id);
//		}
		if (!entry.isEntity()) {
			return ;
		}
		for (DbmMappedField idField : entry.getIdentifyFields()) {
			if (idField.isGeneratedValue()) {
				Object id = generatedIdentifyBeforeInsert(event, idField);
				idField.setValue(entity, id);
			}
		}
	}

	public Object generatedIdentifyBeforeInsert(DbmInsertEvent event, DbmMappedField idField){
		DbmSessionEventSource es = event.getEventSource();
		IdentifierGenerator<?> idGenerator = idField.getIdGenerator();
		Object id = idGenerator.generate(es);
		return id;
	}
	
}
