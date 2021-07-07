package org.onetwo.dbm.event.internal.mysql;


import java.util.List;

import org.onetwo.dbm.event.internal.DbmBatchInsertEventListener;
import org.onetwo.dbm.event.internal.DbmSessionEventSource;
import org.onetwo.dbm.event.spi.DbmBatchInsertEvent;
import org.onetwo.dbm.mapping.DbmMappedEntry;
import org.onetwo.dbm.mapping.JdbcStatementContext;

/*******
 * @author wayshall
 *
 */
public class MySQLBatchInsertOrIgnoreEventListener extends DbmBatchInsertEventListener {

	protected void batchInsert(DbmBatchInsertEvent event, DbmMappedEntry entry, DbmSessionEventSource es) {
		Object entity = event.getObject();
		
		JdbcStatementContext<List<Object[]>> insert = entry.makeMysqlInsertOrIgnore(entity);
		int total = this.executeJdbcUpdate(true, insert.getSql(), insert.getValue(), es, event.getBatchSize());
		event.setUpdateCount(total);
	}
	

}
