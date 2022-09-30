package org.onetwo.dbm.event.internal.oracle;

import java.util.List;

import org.onetwo.dbm.event.internal.DbmInsertEventListener;
import org.onetwo.dbm.event.internal.DbmSessionEventSource;
import org.onetwo.dbm.event.spi.DbmInsertEvent;
import org.onetwo.dbm.mapping.DbmMappedEntry;
import org.onetwo.dbm.mapping.JdbcStatementContext;

public class OracleInsertEventListener extends DbmInsertEventListener {

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
	

}
