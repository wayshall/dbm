package org.onetwo.dbm.jdbc.spi;

import java.beans.PropertyDescriptor;

import org.onetwo.dbm.mapping.DbmMappedField;

public interface ColumnValueGetter {
	
	public Object getColumnValue(int index, PropertyDescriptor pd);

	public Object getColumnValue(int index, DbmMappedField field);
}
