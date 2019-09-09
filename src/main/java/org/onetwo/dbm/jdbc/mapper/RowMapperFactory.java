package org.onetwo.dbm.jdbc.mapper;

import org.onetwo.common.db.dquery.NamedQueryInvokeContext;

public interface RowMapperFactory {
	public DataRowMapper<?> createRowMapper(Class<?> type);
	public DataRowMapper<?> createRowMapper(NamedQueryInvokeContext invokeContext);
	
}
