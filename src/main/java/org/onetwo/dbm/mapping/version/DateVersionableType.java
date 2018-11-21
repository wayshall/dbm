package org.onetwo.dbm.mapping.version;

import java.util.Date;

import org.onetwo.common.db.DataBase;
import org.onetwo.dbm.dialet.DBDialect;

public class DateVersionableType implements VersionableType<Date> {

	@Override
	public boolean isSupportType(DBDialect dbDialect, Class<?> type) {
		return Date.class.isAssignableFrom(type) && dbDialect.getDbmeta().getDataBase()!=DataBase.MySQL;
	}

	@Override
	public Date getVersionValule(Date oldVersion) {
		return new Date();
	}

	@Override
	public boolean isEquals(Date newVersion, Date oldVersion) {
		return newVersion!=null && newVersion.equals(oldVersion);
	}

}
