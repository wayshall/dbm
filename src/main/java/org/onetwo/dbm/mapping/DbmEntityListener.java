package org.onetwo.dbm.mapping;

public interface DbmEntityListener {
	
	public void beforeInsert(Object entity);
	
	public void afterInsert(Object entity);
	
	public void beforeUpdate(Object entity);
	
	public void afterUpdate(Object entity);

}
