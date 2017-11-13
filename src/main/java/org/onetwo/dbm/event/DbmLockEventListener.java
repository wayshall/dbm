package org.onetwo.dbm.event;

import java.util.List;

import org.onetwo.common.utils.LangUtils;
import org.onetwo.dbm.dialet.DBDialect;
import org.onetwo.dbm.dialet.DBDialect.LockInfo;
import org.onetwo.dbm.exception.DbmException;
import org.onetwo.dbm.mapping.DbmMappedEntry;
import org.onetwo.dbm.mapping.JdbcStatementContext;
import org.springframework.jdbc.core.RowMapper;

public class DbmLockEventListener extends AbstractDbmEventListener {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void doEvent(DbmSessionEvent event) {
		DbmLockEvent lockEvent = (DbmLockEvent) event;
		Object entity = event.getObject();
		
		if(LangUtils.isMultiple(entity)){
			throw new DbmException("can not lock multiple object: " + entity.getClass());
		}
		
		DbmSessionEventSource es = event.getEventSource();
		DbmMappedEntry entry = es.getMappedEntryManager().getEntry(event.getEntityClass());
		DBDialect dialect = es.getDialect();
		
		RowMapper rowMapper = es.getSessionFactory().getRowMapper(event.getEntityClass());
		JdbcStatementContext<List<Object[]>> lockContext = entry.makeFetch(entity, true);
		String sql = lockContext.getSql() + dialect.getLockSqlString(new LockInfo(lockEvent.getLock(), lockEvent.getTimeInMillis()));
		
		List list = (List) es.getDbmJdbcOperations().query(sql, lockContext.getValue().get(0), rowMapper);
		if(LangUtils.isNotEmpty(list)){
			lockEvent.setResultObject(list.get(0));
		}
	}

}
