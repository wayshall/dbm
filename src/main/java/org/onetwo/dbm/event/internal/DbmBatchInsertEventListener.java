package org.onetwo.dbm.event.internal;


import java.util.List;

import org.onetwo.common.utils.LangUtils;
import org.onetwo.dbm.event.spi.DbmBatchInsertEvent;
import org.onetwo.dbm.event.spi.DbmInsertEvent;
import org.onetwo.dbm.exception.DbmException;
import org.onetwo.dbm.mapping.DbmMappedEntry;
import org.onetwo.dbm.mapping.JdbcStatementContext;

/*******
 * 和普通insert的区别只在，当实体的id策略是自增时，不会通过Statement#getGeneratedKeys接口获取数据库递增的id值和回写到实体的id
 * 只对mysql有效
 * @author wayshall
 *
 */
public class DbmBatchInsertEventListener extends DbmInsertEventListener {


	@Override
	public void onInsert(DbmInsertEvent event) {
		DbmMappedEntry entry = event.getEventSource().getMappedEntryManager().findEntry(event.getObject());
		if(entry==null){
			event.setUpdateCount(0);
			return ;
		}
		super.onInsert(event);
	}
	
	@Override
	protected void doInsert(DbmInsertEvent event, DbmMappedEntry entry) {
		Object entity = event.getObject();
		if(!LangUtils.isMultiple(entity)){
			throw new DbmException("the args of " + event.getAction() + " must be a Collection or Array!");
		}
		DbmSessionEventSource es = event.getEventSource();
		this.beforeDoInsert(event, entry);
		this.batchInsert((DbmBatchInsertEvent)event, entry, es);
	}
	
	protected void batchInsert(DbmBatchInsertEvent event, DbmMappedEntry entry, DbmSessionEventSource es) {
		Object entity = event.getObject();
		
		JdbcStatementContext<List<Object[]>> insert = entry.makeInsert(entity);
		int total = this.executeJdbcUpdate(true, insert.getSql(), insert.getValue(), es, event.getBatchSize());
		event.setUpdateCount(total);
	}
	
	@Override
	protected void beforeDoInsert(DbmInsertEvent event, DbmMappedEntry entry){
		batchSetIdIfNecessary(event, entry);
	}
	
	static public void batchSetIdIfNecessary(DbmInsertEvent event, DbmMappedEntry entry){
		Object entity = event.getObject();
		
		if(!LangUtils.isMultiple(entity)){
			throw new DbmException("the source object must be a multiple object : " + entity.getClass());
		}
		if(entry.isEntity() && entry.hasGeneratedValueIdField()){
//			Serializable id = null;
			List<Object> list = LangUtils.asList(entity);
			for(Object en : list){
//				id = generatedIdentifyBeforeInsert(event, entry);
				setIdIfNecessary(event.getEventSource(), entry, en);
//				entry.setId(en, id);
			}
		}
	}

}
