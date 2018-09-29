package org.onetwo.dbm.event.internal;

import org.onetwo.dbm.event.spi.DbmInsertOrUpdateEvent;
import org.onetwo.dbm.event.spi.DbmSessionEvent;
import org.onetwo.dbm.exception.EntityInsertException;
import org.onetwo.dbm.mapping.DbmMappedEntry;
import org.springframework.dao.DuplicateKeyException;

public class DbmInsertOrUpdateListener extends AbstractDbmEventListener {

	@Override
	protected int onInnerEventWithSingle(Object entity, DbmSessionEvent event){
		DbmInsertOrUpdateEvent insertOrUpdate = (DbmInsertOrUpdateEvent)event;
		DbmSessionEventSource es = insertOrUpdate.getEventSource();
		DbmMappedEntry entry = es.getMappedEntryManager().getEntry(entity);
		int updateCount = 0;
		
		if(entry.hasIdentifyValue(entity)){
			//如果id有值并且id是自增
			if(entry.getIdentifyField().isGeneratedValue() || entry.getIdentifyField().isIdentityStrategy()){
				if(insertOrUpdate.isDynamicUpdate()){
					updateCount = es.dymanicUpdate(entity);
				}else{
					updateCount = es.update(entity);
				}
			}else{
				try {
					es.insert(entity);
				} catch (EntityInsertException | DuplicateKeyException e) {
					logger.warn("insert error, try to update...");
					if(insertOrUpdate.isDynamicUpdate()){
						updateCount = es.dymanicUpdate(entity);
					}else{
						updateCount = es.update(entity);
					}
				}
			}
		}else{
			updateCount = es.insert(entity);
		}
		return updateCount;
	}
	

}