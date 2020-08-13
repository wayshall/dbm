package org.onetwo.common.db.sqlext;

import java.util.Map;

import org.onetwo.dbm.dialet.DBDialect.LockInfo;
import org.onetwo.dbm.exception.DbmException;

public interface SelectExtQuery extends ExtQueryInner {

	public boolean needSetRange();

	Integer getFirstResult();

	Integer getMaxResults();
	
	void setMaxResults(Integer maxResults);
	
//	boolean isIgnoreQuery();
	Map<Object, Object> getQueryConfig();
	String getCountSql();
	

	boolean isSubQuery();

	void setSubQuery(boolean subQuery);
	
	boolean isCacheable();

	LockInfo getLockInfo();
	
	/*****
	 * 限制返回的结果数量
	 * @author weishao zeng
	 * @param first base from 0
	 * @param size
	 */
	void limit(int first, int size);
	
	void select(String... fields);
	
	default void selectId() {
		throw new DbmException("unsupported operation!");
	}
	
}
