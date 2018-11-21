package org.onetwo.dbm.mapping.version;

import org.onetwo.dbm.dialet.DBDialect;

public interface VersionableType<T> {
	
	public boolean isSupportType(DBDialect dbDialect, Class<?> type);
	/***
	 * 获取新的版本值
	 * @author weishao zeng
	 * @param oldVersion
	 * @return
	 */
	public T getVersionValule(T oldVersion);
	public boolean isEquals(T newVersion, T oldVersion);
}
