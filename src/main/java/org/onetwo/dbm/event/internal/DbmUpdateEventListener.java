package org.onetwo.dbm.event.internal;

import java.util.Collection;
import java.util.List;

import org.onetwo.common.utils.CUtils;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.dbm.event.spi.DbmUpdateEvent;
import org.onetwo.dbm.exception.EntityNotFoundException;
import org.onetwo.dbm.exception.EntityVersionException;
import org.onetwo.dbm.mapping.DbmMappedEntry;
import org.onetwo.dbm.mapping.DbmMappedField;
import org.onetwo.dbm.mapping.JdbcStatementContext;
import org.onetwo.dbm.utils.Dbms;

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
				Collection<Object> entityCol = CUtils.toCollection(entity);
				for(Object e : entityCol){
					throwIfMultiple(entity, e);
					count += updateSingleEntity(false, es, entry, e);
				}
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
		Object currentTransactionVersion = null;
		Object entityVersion = null;

		if(entry.isVersionControll()){
			currentTransactionVersion = getLastVersionWithNewTransaction(es, entry, singleEntity);
			entityVersion = entry.getVersionField().getValue(singleEntity);
			DbmMappedField versionField = entry.getVersionField();
			
			// 如果一个实体被获取后，脱离了事务，然后又重新使用update方法，则会出现实体版本和当前事务版本不一致的情况；
			if(!versionField.getVersionableType().isEquals(entityVersion, currentTransactionVersion)){
				throw new EntityVersionException(entry.getEntityClass(), entry.getId(singleEntity), entityVersion, currentTransactionVersion);
			}
		}
		
		JdbcStatementContext<List<Object[]>> update = dymanic?entry.makeDymanicUpdate(singleEntity):entry.makeUpdate(singleEntity);
		int count = this.executeJdbcUpdate(false, update.getSql(), update.getValue(), es);
		
		if(count<1){
			if(currentTransactionVersion!=null){
//				Object entityVersion = update.getSqlBuilder().getVersionValue(update.getValue().get(0));
//				Object entityVersion = entry.getVersionField().getValue(singleEntity);
				throw new EntityVersionException(entry.getEntityClass(), entry.getId(singleEntity), entityVersion, currentTransactionVersion);
			}else{
				throw new EntityNotFoundException("update count is " + count + ".", singleEntity.getClass(), entry.getId(singleEntity));
			}
		}
		
		this.updateEntityVersionIfNecessary(update.getSqlBuilder(), update.getValue().get(0), singleEntity);
		
		return count;
	}
	
	/***
	 * 如果在同一个事务里，实际上得到的version还是旧的，只是防止程序员自己修改version字段
	 * 
	 * @author weishao zeng
	 * @param es
	 * @param entry
	 * @param singleEntity
	 * @return
	 */
	private Object getLastVersion(DbmSessionEventSource es, DbmMappedEntry entry, Object singleEntity) {
		DbmMappedField versionField = entry.getVersionField();
		JdbcStatementContext<Object[]> versionContext = entry.makeSelectVersion(singleEntity);
		Object last = es.getDbmJdbcOperations().queryForObject(versionContext.getSql(), versionField.getColumnType(), entry.getId(singleEntity));
		return last;
	}
	
	/***
	 * 使用新的事务获取最新版本值
	 * 
	 * @author weishao zeng
	 * @param es
	 * @param entry
	 * @param singleEntity
	 * @return
	 */
	private Object getLastVersionWithNewTransaction(DbmSessionEventSource es, DbmMappedEntry entry, Object singleEntity) {
		return Dbms.doInRequiresNewPropagation(es, t->{
			return getLastVersion(es, entry, singleEntity);
		});
	}

}
