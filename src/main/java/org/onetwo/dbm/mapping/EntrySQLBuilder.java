package org.onetwo.dbm.mapping;

import java.util.List;

import org.onetwo.dbm.dialet.DBDialect;
import org.onetwo.dbm.mapping.SQLBuilderFactory.SqlBuilderType;

public interface EntrySQLBuilder {

	String build();

	String getSql();

	SqlBuilderType getType();
	
	DbmMappedEntryMeta getEntry();
	
	List<DbmMappedFieldValue> getFields();
	
	List<String> fieldNameToString(String alias);
	
	Object getVersionValue(Object[] updateValues);
	
	List<DbmMappedFieldValue> getWhereCauseFields();
	
	EntrySQLBuilder append(DbmMappedField column);
	
	EntrySQLBuilder appendWhere(DbmMappedField column);
	
	EntrySQLBuilder appendWhere(DbmMappedFieldValue column);
	
	DBDialect getDialet();
	
	
//	void setLock(LockInfo lock);

}