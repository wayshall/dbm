package org.onetwo.dbm.mapping.version;

import org.onetwo.dbm.dialet.DBDialect;

public class IntegerVersionableType implements VersionableType<Integer> {

	@Override
	public boolean isSupport(DBDialect dbDialect, Class<?> type) {
		return type==Integer.class || type==int.class;
	}

	@Override
	public Integer getVersionValule(Integer oldVersion) {
		if(oldVersion==null)
			return 1;
		else
			return oldVersion + 1;
	}

	@Override
	public boolean isEquals(Integer newVersion, Integer oldVersion) {
		if (newVersion==null && oldVersion==null) {
			return true;
		}
		return newVersion!=null && newVersion.equals(oldVersion);
	}

}
