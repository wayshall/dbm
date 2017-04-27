package org.onetwo.dbm.event;

import org.onetwo.dbm.mapping.DbmMappedField;


public interface DbmEntityFieldListener {
	
	public Object beforeFieldInsert(DbmMappedField field, Object fieldValue);
	
	public Object beforeFieldUpdate(DbmMappedField field, Object fieldValue);
	
}
