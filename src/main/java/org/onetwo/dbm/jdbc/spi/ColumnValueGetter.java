package org.onetwo.dbm.jdbc.spi;

import org.onetwo.dbm.mapping.DbmMappedField;

public interface ColumnValueGetter {
	
	public Object getColumnValue(int index, Class<?> requiredType);

	public Object getColumnValue(int index, DbmMappedField field);
}
