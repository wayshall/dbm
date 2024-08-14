package org.onetwo.common.db.spi;

import java.util.List;
import java.util.Map;

import org.onetwo.common.utils.Page;
import org.onetwo.dbm.dialet.DBDialect.LockInfo;
import org.springframework.jdbc.core.RowMapper;

@SuppressWarnings("rawtypes")
public interface QueryWrapper {
 
	public int executeUpdate();

	public <T> List<T> getResultList();

	public <T> T getSingleResult();

	public QueryWrapper setFirstResult(int startPosition);

	public QueryWrapper setMaxResults(int maxResult);

	public QueryWrapper setParameter(int position, Object value);

	public QueryWrapper setParameter(String name, Object value);
	
	public QueryWrapper setParameters(Map<String, Object> params);
	
	public QueryWrapper setParameters(List<Object> params);
	
	public QueryWrapper setParameters(Object[] params);
	
	public QueryWrapper setPageParameter(final Page page);
	
	/***
	 * 
	 * @author weishao zeng
	 * @param first  from 0
	 * @param size
	 * @return
	 */
	public QueryWrapper setLimited(final Integer first, final Integer size);

	QueryWrapper setLockInfo(LockInfo lockInfo);
	
//	LockInfo getLockInfo();
	
	Map<?, Object> getParameters();
	
	public <T> T getRawQuery(Class<T> clazz);
	
	public QueryWrapper setQueryConfig(Map<Object, Object> configs);
	
//	public DataQuery setFlushMode(FlushModeType flushMode);
	
	/*public boolean isCacheable();

	public void setCacheable(boolean cacheable);*/
	
	public void setRowMapper(RowMapper<?> rowMapper);
	
	public <T> T unwarp(Class<T> clazz);
	

	boolean isUseAutoLimitSqlIfPagination();

	void setUseAutoLimitSqlIfPagination(boolean useAutoLimitSqlIfPagination);
	

}