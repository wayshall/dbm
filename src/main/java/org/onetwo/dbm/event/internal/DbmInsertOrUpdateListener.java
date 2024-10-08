package org.onetwo.dbm.event.internal;

import java.util.Date;
import java.util.Map;

import org.onetwo.common.db.TimeRecordableEntity;
import org.onetwo.common.db.sqlext.ExtQuery.K;
import org.onetwo.dbm.event.spi.DbmInsertOrUpdateEvent;
import org.onetwo.dbm.event.spi.DbmSessionEvent;
import org.onetwo.dbm.exception.EntityInsertException;
import org.onetwo.dbm.mapping.DbmMappedEntry;
import org.onetwo.dbm.mapping.DbmMappedField;
import org.springframework.dao.DuplicateKeyException;

import com.google.common.collect.Maps;

/****
 * insertOrUpdate支持对象为集合类型的参数
 * @author way
 *
 */
public class DbmInsertOrUpdateListener extends AbstractDbmEventListener {

	@Override
	protected int onInnerEventWithSingle(Object entity, DbmSessionEvent event){
		DbmInsertOrUpdateEvent insertOrUpdate = (DbmInsertOrUpdateEvent)event;
		DbmSessionEventSource es = insertOrUpdate.getEventSource();
		DbmMappedEntry entry = es.getMappedEntryManager().getEntry(entity);
		int updateCount = 0;
		
		if(entry.hasIdentifyValue(entity)){
			// 如果id有值并且id是自增或者自动生成，则首先尝试执行update操作
//			if(entry.getIdentifyFields().isGeneratedValue() || entry.getIdentifyField().isIdentityStrategy()){
//			if(entry.hasGeneratedValueIdField() || entry.hasIdentityStrategyField()){
//				try {
//					if(insertOrUpdate.isDynamicUpdate()){
//						updateCount = es.dymanicUpdate(entity);
//					}else{
//						updateCount = es.update(entity);
//					}
//				} catch (EntityNotFoundException e) {
//					// update失败，则尝试插入
////					insert(insertOrUpdate, entry, entity);
//					updateCount = es.insert(entity);
//				}
//			}else{
//				updateCount = insert(insertOrUpdate, entry, entity);
//			}
			
			Map<Object, Object> query = Maps.newHashMap();
			query.put(K.COUNT, "1");
			for (DbmMappedField field : entry.getIdentifyFields()) {
				query.put(field.getName(), field.getValue(entity));
			}
			Number count = es.countByProperties(entry.getEntityClass(), query);
			if (count!=null && count.intValue()>0) {
				if(insertOrUpdate.isDynamicUpdate()){
					updateCount = es.dymanicUpdate(entity);
				}else{
					updateCount = es.update(entity);
				}
			} else {
				updateCount = insert(insertOrUpdate, entry, entity);
			}
		}else{
			updateCount = es.insert(entity);
		}
		return updateCount;
	}
	
	private int insert(DbmInsertOrUpdateEvent insertOrUpdate, DbmMappedEntry entry, Object entity) {
		// 插入前需要保存version字段的当前值，因为insert的时候可能会更改了
		Object versionValue = null;
		if(entry.isVersionControll()) {
			versionValue = entry.getVersionField().getValue(entity);
		}
		
		Date createAt = null;
		TimeRecordableEntity timeEntity = null;
		if (TimeRecordableEntity.class.isInstance(entity)) {
			timeEntity = (TimeRecordableEntity) entity;
			createAt = timeEntity.getCreateAt();
		}
		
		int updateCount = 0;
		try {
			insertOrUpdate.getEventSource().insert(entity);
		} catch (EntityInsertException | DuplicateKeyException e) {
			logger.warn("insert error, try to update...");
			// 失败后把当前version值设置回去
			if(entry.isVersionControll()) {
				entry.getVersionField().setValue(entity, versionValue);
			}
			// 失败后设置回createAt
			if (timeEntity!=null) {
				timeEntity.setCreateAt(createAt);
			}
			// DuplicateKeyException异常时，转为update
			// TODO：这里有点问题，DuplicateKeyException异常，不一定就是主键重复，可能是其它唯一约束冲突，此时转为（通过id的）update，也一样是错误的
			if(insertOrUpdate.isDynamicUpdate()){
				updateCount = insertOrUpdate.getEventSource().dymanicUpdate(entity);
			}else{
				updateCount = insertOrUpdate.getEventSource().update(entity);
			}
		}
		return updateCount;
	}

}
