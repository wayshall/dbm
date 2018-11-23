package org.onetwo.dbm.mapping.version;

import org.onetwo.dbm.dialet.DBDialect;

public class LongVersionableType implements VersionableType<Long> {

	@Override
	public boolean isSupport(DBDialect dbDialect, Class<?> type) {
		return type==Long.class || type==long.class;
	}

	@Override
	public Long getVersionValule(Long oldVersion) {
		if(oldVersion==null)
			return 1L;
		else
			return oldVersion + 1;
	}

	@Override
	public boolean isEquals(Long newVersion, Long oldVersion) {
		return newVersion!=null && newVersion.equals(oldVersion);
	}

}
