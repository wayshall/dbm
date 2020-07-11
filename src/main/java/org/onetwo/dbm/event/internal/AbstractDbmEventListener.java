package org.onetwo.dbm.event.internal;

import java.util.Collection;
import java.util.List;

import org.onetwo.common.log.JFishLoggerFactory;
import org.onetwo.common.utils.ArrayUtils;
import org.onetwo.common.utils.CUtils;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.common.utils.MathUtils;
import org.onetwo.dbm.event.spi.DbmEventAction;
import org.onetwo.dbm.event.spi.DbmEventListener;
import org.onetwo.dbm.event.spi.DbmInsertOrUpdateEvent;
import org.onetwo.dbm.event.spi.DbmSessionEvent;
import org.onetwo.dbm.exception.DbmException;
import org.onetwo.dbm.exception.EntityNotFoundException;
import org.onetwo.dbm.exception.EntityVersionException;
import org.onetwo.dbm.jdbc.internal.SimpleArgsPreparedStatementCreator;
import org.onetwo.dbm.mapping.DbmConfig;
import org.onetwo.dbm.mapping.DbmEntityListener;
import org.onetwo.dbm.mapping.DbmMappedEntry;
import org.onetwo.dbm.mapping.DbmMappedEntryMeta;
import org.onetwo.dbm.mapping.DbmMappedField;
import org.onetwo.dbm.mapping.EntrySQLBuilder;
import org.onetwo.dbm.mapping.JdbcStatementContext;
import org.onetwo.dbm.utils.DbmUtils;
import org.slf4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;

@SuppressWarnings("unchecked")
abstract public class AbstractDbmEventListener implements DbmEventListener<DbmSessionEventSource, DbmSessionEvent> {
	
	protected final Logger logger = JFishLoggerFactory.getLogger(this.getClass());
	
	
	@Override
	public void doEvent(DbmSessionEvent event) {
		Object entity = event.getObject();
		DbmInsertOrUpdateEvent insertOrUpdate = (DbmInsertOrUpdateEvent) event;

		int updateCount = 0;
		if(LangUtils.isMultiple(entity)){
			Collection<Object> entities = CUtils.toCollection(entity);
			for(Object obj : entities){
				if(obj==null)
					continue;
				throwIfMultiple(entities, obj);
				updateCount += this.onInnerEventWithSingle(obj, insertOrUpdate);
			}
		}else{
			updateCount = onInnerEventWithSingle(entity, insertOrUpdate);
		}
		event.setUpdateCount(updateCount);
	}
	
	protected int onInnerEventWithSingle(Object entity, DbmSessionEvent event){
		throw new UnsupportedOperationException();
	}
	
	protected void throwIfMultiple(Object parent, Object entity){
		if(LangUtils.isMultiple(entity)){
			String msg = "element of "+(parent==null?"container":parent)+" can not be a multiple object, element: " + entity;
			throw new DbmException(msg);
		}
	}

	
	protected <T extends DbmMappedField> void processRelatedField(String[] relatedFields, Collection<? extends DbmMappedField> mappedFields, MappedFieldProcessor<T> processor){
		for(DbmMappedField field : mappedFields){
			if(ArrayUtils.contains(relatedFields, field.getName())){
				processor.execute((T)field);
			}
		}
	}
	
	protected boolean isUseBatchUpdate(List<?> args, DbmSessionEventSource es){
		DbmConfig dbc = es.getDataBaseConfig();
		return dbc.isUseBatchOptimize() && args.size()>dbc.getUseBatchThreshold();
	}
	

	protected int executeJdbcUpdate(DbmSessionEventSource es, JdbcStatementContext<List<Object[]>> update){
		return executeJdbcUpdate(update.getSql(), update.getValue(), es);
	}
	
	/********
	 * 会更加配置决定是否调用jdbc的executeBatch接口
	 * @param event
	 * @param sql
	 * @param args
	 * @param es
	 * @return
	 */
	protected int executeJdbcUpdate(String sql, List<Object[]> args, DbmSessionEventSource es){
		return executeJdbcUpdate(isUseBatchUpdate(args, es), sql, args, es);
	}
	
	/******
	 * 如果使用批量处理，因为某些驱动的实现机制，比如oracle，不能根据返回值来判断是否更新成功
	 * @param userBatch
	 * @param sql
	 * @param args
	 * @param es
	 * @return
	 */
	protected int executeJdbcUpdate(boolean userBatch, String sql, List<Object[]> args, DbmSessionEventSource es){
		return executeJdbcUpdate(userBatch, sql, args, es, es.getDataBaseConfig().getProcessSizePerBatch());
	}
	/***
	 * 
	 * @author weishao zeng
	 * @param userBatch
	 * @param sql
	 * @param args
	 * @param es
	 * @param configBatchSize 批量处理时，每次提交的数据量
	 * @return
	 */
	protected int executeJdbcUpdate(boolean userBatch, String sql, List<Object[]> args, DbmSessionEventSource es, Integer configBatchSize){
		int count = 0;
		if(userBatch){
//			int[] ups = es.getJFishJdbcTemplate().batchUpdate(sql, args);
			int batchSize = configBatchSize!=null?configBatchSize:es.getDataBaseConfig().getProcessSizePerBatch();
			int[][] ups = es.getDbmJdbcOperations().batchUpdateWith(sql, args, batchSize/*, new ParameterizedPreparedStatementSetter<Object[]>(){

				@Override
				public void setValues(PreparedStatement ps, Object[] argument) throws SQLException {
					JdbcUtils.setValues(ps, argument);
				}
				
			}*/);
			for(int[] up : ups)
				count += MathUtils.sum(up);
		}else{
			for(Object[] arg : args){
				count += es.getDbmJdbcOperations().updateWith(new SimpleArgsPreparedStatementCreator(sql, arg));
			}
		}
		return count;
	}
	

	protected void executeDbmEntityListener(boolean before, DbmSessionEvent jfishEvent, Object entities, List<DbmEntityListener> listeners){
		if(LangUtils.isMultiple(entities)){
			List<?> list = LangUtils.asList(entities);
			for(Object entity : list){
				this.executeJFishEntityListenerForSingle(before, jfishEvent, entity, listeners);
			}
		}else{
			this.executeJFishEntityListenerForSingle(before, jfishEvent, entities, listeners);
		}
	}

	protected void executeJFishEntityListenerForSingle(boolean before, DbmSessionEvent jfishEvent, Object entity, List<DbmEntityListener> listeners){
		if(LangUtils.isEmpty(listeners))
			return ;
		if(before){
			if(jfishEvent.getAction()==DbmEventAction.insert){
				for(DbmEntityListener jel : listeners){
					jel.beforeInsert(entity);
				}
			}
			else if(jfishEvent.getAction()==DbmEventAction.update){
				for(DbmEntityListener jel : listeners){
					jel.beforeUpdate(entity);
				}
			}
		}else{
			if(jfishEvent.getAction()==DbmEventAction.insert){
				for(DbmEntityListener jel : listeners){
					jel.afterInsert(entity);
				}
			}
			else if(jfishEvent.getAction()==DbmEventAction.update){
				for(DbmEntityListener jel : listeners){
					jel.afterUpdate(entity);
				}
			}
		}
	}
	

	protected void updateEntityVersionIfNecessary(EntrySQLBuilder builder, Object[] updateValues, Object singleEntity){
		DbmMappedEntryMeta entry = builder.getEntry();
		if(entry.isVersionControll()){
			Object versionValue = builder.getVersionValue(updateValues);
			entry.getVersionField().setValue(singleEntity, versionValue);
		}
	}
	
	protected void throwIfEffectiveCountError(String operation, int expectCount, int effectiveCount){
		DbmUtils.throwIfEffectiveCountError(operation + " error.", expectCount, effectiveCount);
	}
	
	protected final Object checkEntityLastVersion(DbmSessionEventSource es, DbmMappedEntry entry, Object singleEntity) {
		Object currentTransactionVersion = null;
		if(entry.isVersionControll()){
			currentTransactionVersion = getLastVersion(es, entry, singleEntity);
			Object entityVersion = entry.getVersionValue(singleEntity);
			DbmMappedField versionField = entry.getVersionField();
			
			if(!versionField.getVersionableType().isEquals(entityVersion, currentTransactionVersion)){
				throw new EntityVersionException(entry.getEntityClass(), entry.getId(singleEntity), entityVersion, currentTransactionVersion);
			}
		}
		return currentTransactionVersion;
	}
	

	/***
	 * 在不可重复读事务里，返回数据库最新版本值
	 * 如果在一个可重复读的事务里，实际上得到的version还是旧的，只是防止程序员自己修改version字段
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
		
		Object id = entry.getId(singleEntity);
		try {
			Object last = es.getDbmJdbcOperations().queryForObject(versionContext.getSql(), versionField.getColumnType(), id);
			return last;
		} catch (EmptyResultDataAccessException e) {
			throw new EntityNotFoundException("get entity version error: ", entry.getEntityClass(), id);
		}
	}
	
	/***
	 * 使用新的事务获取最新版本值
	 * 不应该使用这个方法，update时保证即可
	 * @author weishao zeng
	 * @param es
	 * @param entry
	 * @param singleEntity
	 * @return
	 
	@SuppressWarnings("unused")
	@Deprecated
	private Object getLastVersionWithNewTransaction(DbmSessionEventSource es, DbmMappedEntry entry, Object singleEntity) {
		return Dbms.doInRequiresNewPropagation(es, t->{
			return getLastVersion(es, entry, singleEntity);
		});
	}*/
}
