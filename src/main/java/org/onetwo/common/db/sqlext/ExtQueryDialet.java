package org.onetwo.common.db.sqlext;

import org.onetwo.dbm.dialet.DBDialect.LockInfo;

public interface ExtQueryDialet {

//	public String getFieldName(String field);

	public String getPlaceHolder(int position);
	
	public String getNamedPlaceHolder(String name, int position);
	
	public String getNullsOrderby(String nullsOrder);
	
	public String getLockSqlString(LockInfo lock);
}
