package org.onetwo.dbm.event.internal;

import java.util.List;

import org.onetwo.common.utils.LangUtils;
import org.onetwo.dbm.event.spi.DbmInsertEvent;
import org.onetwo.dbm.exception.DbmException;
import org.onetwo.dbm.exception.EntityInsertException;
import org.onetwo.dbm.jdbc.internal.SimpleArgsPreparedStatementCreator;
import org.onetwo.dbm.mapping.DbmMappedEntry;
import org.onetwo.dbm.mapping.JdbcStatementContext;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

/*******
 * 
 * @author wayshall
 *
 */
public class DbmInsertEventListener extends InsertEventListener{
	
	

	protected void beforeDoInsert(DbmInsertEvent event, DbmMappedEntry entry){
		Object entity = event.getObject();
		
		throwIfEntityIsMultiple(entity);
		setIdIfNecessary(event.getEventSource(), entry, entity);

		/*if(entry.isEntity() && entry.getIdentifyField().isGeneratedValue()){
			Serializable id = null;
			if(LangUtils.isMultiple(entity)){
				List<Object> list = LangUtils.asList(entity);
				for(Object en : list){
					id = generatedIdentifyBeforeInsert(event, entry);
					entry.setId(en, id);
				}
			}else{
				id = generatedIdentifyBeforeInsert(event, entry);
				entry.setId(entity, id);
			}
		}*/
	}
	
	/***
	 * 另外提供了batchInsert，batchInsert的参数为集合类型
	 * 
	 * @author weishao zeng
	 * @param entity
	 */
	private void throwIfEntityIsMultiple(Object entity){
		if(LangUtils.isMultiple(entity)){
			throw new DbmException("the source object can not be a multiple object : "+entity.getClass());
		}
	}
	
	/****
	 * 显式调用insert，并且fetchId为false，才会转为批量插入；
	 * 已经修改为实际不会自动优化
	 * @see #throwIfEntityIsMultiple
	 */
	protected void doInsert(DbmInsertEvent event, DbmMappedEntry entry) {
		DbmSessionEventSource es = event.getEventSource();
		this.beforeDoInsert(event, entry);
		
		Object entity = event.getObject();
		
		JdbcStatementContext<List<Object[]>> insert = entry.makeInsert(entity);
		if(insert==null)
			return ;
		String sql = insert.getSql();
		List<Object[]> args = insert.getValue();
		List<Object> objects = LangUtils.asList(entity);
		
		int updateCount = 0;
		if (event.isFetchId()) {
			// 自动生成id的话，无法使用批量插入优化
			if (entry.hasIdentityStrategyField()) { 
				int index = 0;
				for (Object[] arg : args) {
					KeyHolder keyHolder = new GeneratedKeyHolder();
					updateCount += es.getDbmJdbcOperations().updateWith(new SimpleArgsPreparedStatementCreator(sql, arg), keyHolder);
					if (keyHolder.getKey()!=null) {
						//TODO 如果有多个自动生成字段，这里还需要处理
						entry.setId(objects.get(index++), keyHolder.getKey());
					}
				}
			}else{
				updateCount += executeJdbcUpdate(sql, args, es);
			}
		} else {
			updateCount += executeJdbcUpdate(sql, args, es);
		}

		if(updateCount<1 && !isUseBatchUpdate(args, es)){
			throw new EntityInsertException(entity, objects.size(), updateCount);
		}
		
		event.setUpdateCount(updateCount);
	}
	
//	@SuppressWarnings("unchecked")
	
}
