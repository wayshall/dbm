package org.onetwo.dbm.event.oracle;

import java.util.List;

import org.onetwo.common.utils.LangUtils;
import org.onetwo.dbm.event.DbmInsertEvent;
import org.onetwo.dbm.event.DbmInsertEventListener;
import org.onetwo.dbm.event.DbmSessionEventSource;
import org.onetwo.dbm.id.IdGenerator;
import org.onetwo.dbm.mapping.DbmMappedEntry;
import org.onetwo.dbm.mapping.JdbcStatementContext;

public class OracleInsertEventListener extends DbmInsertEventListener {

	@Override
	protected void beforeDoInsert(DbmInsertEvent event, DbmMappedEntry entry){
		Object entity = event.getObject();

		if(entry.isEntity() && entry.getIdentifyField().isGeneratedValueFetchBeforeInsert()){
			Long id = null;
			if(LangUtils.isMultiple(entity)){
				List<Object> list = LangUtils.asList(entity);
				for(Object en : list){
					id = fetchIdentifyBeforeInsert(event, entry);
					entry.setId(en, id);
				}
			}else{
				id = fetchIdentifyBeforeInsert(event, entry);
				entry.setId(entity, id);
			}
		}
		
	}
	/********
	 * 不可以根据更新数量的数目来确定是否成功
	 * oracle如果使用了不符合jdbc3.0规范的本地批量更新机制（BatchPerformanceWorkaround=true），
	 * 每次插入的返回值都是{@linkplain java.sql.Statement#SUCCESS_NO_INFO -2}
	 */
	@Override
	protected void doInsert(DbmInsertEvent event, DbmMappedEntry entry) {
		DbmSessionEventSource es = event.getEventSource();
		this.beforeDoInsert(event, entry);
		Object entity = event.getObject();
		JdbcStatementContext<List<Object[]>> insert = entry.makeInsert(entity);
		/*
		String sql = insert.getSql();
		List<Object[]> args = insert.getValue();
		
		int count = executeJdbcUpdate(event, sql, args, es);*/
		int count = this.executeJdbcUpdate(es, insert);
		
		event.setUpdateCount(count);
	}
	
	@SuppressWarnings("unchecked")
	public Long fetchIdentifyBeforeInsert(DbmInsertEvent event, DbmMappedEntry entry){
		DbmSessionEventSource es = event.getEventSource();
		IdGenerator<Long> idGenerator = (IdGenerator<Long>)entry.getIdentifyField().getIdGenerator();
		Long id = idGenerator.generate(es);
		return id;
	}

}
