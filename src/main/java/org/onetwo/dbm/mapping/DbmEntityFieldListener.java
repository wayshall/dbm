package org.onetwo.dbm.mapping;



public interface DbmEntityFieldListener {
	
	public Object beforeFieldInsert(DbmMappedField field, Object fieldValue);
	
	public Object beforeFieldUpdate(DbmMappedField field, Object fieldValue);
	
}
