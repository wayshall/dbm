package org.onetwo.dbm.mapping;

import java.util.List;

import org.onetwo.dbm.mapping.SQLBuilderFactory.SqlBuilderType;

public interface EntrySQLBuilder {

	String build();

	String getSql();

	SqlBuilderType getType();
	
	DbmMappedEntryMeta getEntry();
	
	Object getVersionValue(Object[] updateValues);
	
	List<DbmMappedFieldValue> getWhereCauseFields();
	
//	void setLock(LockInfo lock);

}