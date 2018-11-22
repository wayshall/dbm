package org.onetwo.dbm.mapping.version;

import org.onetwo.dbm.dialet.DBDialect;

public interface VersionableType<T> {
	
	/***
	 * 是否支持当前字段类型
	 * 
	 * @author weishao zeng
	 * @param dbDialect
	 * @param fieldType
	 * @return
	 */
	boolean isSupport(DBDialect dbDialect, Class<?> fieldType);
	
	/***
	 * 获取新的版本值
	 * 
	 * @author weishao zeng
	 * @param oldVersion
	 * @return
	 */
	T getVersionValule(T oldVersion);
	
	/***
	 * 新旧值是否相等
	 * 
	 * @author weishao zeng
	 * @param newVersion
	 * @param oldVersion
	 * @return
	 */
	boolean isEquals(T newVersion, T oldVersion);
	
}
