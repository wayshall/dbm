package org.onetwo.dbm.event.internal;

import java.util.Collection;
import java.util.List;

import org.onetwo.common.utils.CUtils;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.dbm.event.spi.DbmUpdateEvent;
import org.onetwo.dbm.exception.EntityNotFoundException;
import org.onetwo.dbm.exception.EntityVersionException;
import org.onetwo.dbm.mapping.DbmMappedEntry;
import org.onetwo.dbm.mapping.JdbcStatementContext;
import org.springframework.jdbc.core.SqlParameterValue;

/*****
 * 
 * @author wayshall
 *
 */
public class DbmUpdateEventListener extends UpdateEventListener {
	
//	private SimpleBeanCopier copier = BeanCopierBuilder.newBuilder().build();

	/*****
	 * 不是调用批量接口更新的，取用循环插入的方式，通过调用updateSingleEntity方法来检查是否更新成功！
	 */
	@Override
	protected void doUpdate(DbmUpdateEvent event, DbmMappedEntry entry){
		Object entity = event.getObject();
		DbmSessionEventSource es = event.getEventSource();
//		JdbcStatementContext<List<Object[]>> update = null;
		int count = 0;
		if(event.isDynamicUpdate()){
			if(LangUtils.isMultiple(entity)){
				Collection<Object> entityCol = CUtils.toCollection(entity);
				for(Object e : entityCol){
					throwIfMultiple(entity, e);
					count += updateSingleEntity(true, es, entry, e);
				}
			}else{
				count += this.updateSingleEntity(true, es, entry, entity);
			}
		}else{
//			count = this.executeJdbcUpdate(es, entry.makeUpdate(entity));
			if(LangUtils.isMultiple(entity)){
				/*Collection<Object> entityCol = CUtils.toCollection(entity);
				for(Object e : entityCol){
					throwIfMultiple(entity, e);
					count += updateSingleEntity(false, es, entry, e);
				}*/
				// 当传入多个实体的时候，根据配置决定是否启用批量操作
				JdbcStatementContext<List<SqlParameterValue[]>> updates = entry.makeUpdate(entity);
				count = this.executeJdbcUpdate(es, updates);
			}else{
				count = this.updateSingleEntity(false, es, entry, entity);
			}
		}
		if(count==0){
			logger.warn("dmb update count is 0, it may be wrong!");
		}
		event.setUpdateCount(count);
	}

	/*********
	 * 更新单个实体，如果更新条数少于1，则表示更新失败，抛出{@link EntityNotFoundException EntityNotFoundException}
	 * @param dymanic
	 * @param es
	 * @param entry
	 * @param singleEntity
	 * @return
	 */
	private int updateSingleEntity(boolean dymanic, DbmSessionEventSource es, DbmMappedEntry entry, Object singleEntity){
		checkEntityLastVersion(es, entry, singleEntity);
		JdbcStatementContext<List<SqlParameterValue[]>> update = dymanic?entry.makeDymanicUpdate(singleEntity):entry.makeUpdate(singleEntity);
		int count = this.executeJdbcUpdate(false, update.getSql(), update.getValue(), es);
		
		if(count<1){
			if(entry.isVersionControll()){
				throw new EntityVersionException(entry.getEntityClass(), entry.getId(singleEntity), entry.getVersionValue(singleEntity));
			}else{
				throw new EntityNotFoundException("update count is " + count + ".", singleEntity.getClass(), entry.getId(singleEntity));
			}
		}
		
		this.updateEntityVersionIfNecessary(update.getSqlBuilder(), update.getValue().get(0), singleEntity);
		
		return count;
	}
	

}
