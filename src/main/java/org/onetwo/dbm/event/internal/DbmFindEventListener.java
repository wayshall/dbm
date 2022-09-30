package org.onetwo.dbm.event.internal;

import java.util.List;

import org.onetwo.common.utils.LangUtils;
import org.onetwo.dbm.event.spi.DbmFindEvent;
import org.onetwo.dbm.event.spi.DbmSessionEvent;
import org.onetwo.dbm.mapping.DbmMappedEntry;
import org.onetwo.dbm.mapping.JdbcStatementContext;
import org.springframework.jdbc.core.RowMapper;

public class DbmFindEventListener extends AbstractDbmEventListener {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void doEvent(DbmSessionEvent event) {
		DbmFindEvent findEvent = (DbmFindEvent) event;
		Object entity = event.getObject();
		DbmSessionEventSource es = event.getEventSource();
		DbmMappedEntry entry = es.getMappedEntryManager().getEntry(event.getEntityClass());
//		findEvent.setJoined(entry.isJoined());

		RowMapper rowMapper = es.getSessionFactory().getRowMapper(event.getEntityClass());
		if(findEvent.isFindAll()){
			JdbcStatementContext<Object[]> fetch = entry.makeFetchAll();
			List list = (List) es.getDbmJdbcOperations().query(fetch.getSql(), fetch.getValue(), rowMapper);
			findEvent.setResultObject(list);
		}else{
//			JdbcStatementContext<List<Object[]>> fetch = entry.makeFetch(entity, !findEvent.isJoined());
			JdbcStatementContext<List<Object[]>> fetch = entry.makeFetch(entity, true);
			for(Object[] args : fetch.getValue()){
				List list = (List) es.getDbmJdbcOperations().query(fetch.getSql(), args, rowMapper);
				if(LangUtils.isNotEmpty(list))
					findEvent.setResultObject(list.get(0));
			}
		}
		
		/*String sql = entry.getStaticFetchSql();
		
		RowMapper rowMapper = es.getDefaultRowMapper(event.getEntityClass(), false);
		List list = (List)es.getJFishJdbcTemplate().query(sql, new Object[]{entity}, rowMapper);
		if(LangUtils.isNotEmpty(list))
			findEvent.setResultObject(list.get(0));*/
	}

}
