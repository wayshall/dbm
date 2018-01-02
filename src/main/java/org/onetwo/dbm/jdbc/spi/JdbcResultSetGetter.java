package org.onetwo.dbm.jdbc.spi;

import org.onetwo.dbm.mapping.DbmMappedField;
import org.springframework.jdbc.support.rowset.ResultSetWrappingSqlRowSet;

public interface JdbcResultSetGetter {
	
//	public Object getColumnValue(ResultSetWrappingSqlRowSet rs, int index, PropertyDescriptor pd);
	public Object getColumnValue(ResultSetWrappingSqlRowSet rs, int index, Class<?> requiredType);

	public Object getColumnValue(ResultSetWrappingSqlRowSet rs, int index, DbmMappedField field);
}
