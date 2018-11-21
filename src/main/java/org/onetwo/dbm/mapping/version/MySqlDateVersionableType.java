package org.onetwo.dbm.mapping.version;

import java.util.Date;

import org.onetwo.common.db.DataBase;
import org.onetwo.dbm.dialet.DBDialect;

public class MySqlDateVersionableType extends DateVersionableType {

	@Override
	public boolean isSupportType(DBDialect dbDialect, Class<?> type) {
		return Date.class.isAssignableFrom(type) && dbDialect.getDbmeta().getDataBase()==DataBase.MySQL;
	}

	@Override
	public Date getVersionValule(Date oldVersion) {
		return new Date(new Date().getTime()/1000*1000);
	}

	/*@Override
	public boolean isEquals(Date newVersion, Date lastVersion) {
		return newVersion!=null && new Date(newVersion.getTime()/1000*1000).equals(new Date(lastVersion.getTime()/1000*1000));
	}*/

}
