package org.onetwo.common.db.spi;

import java.util.List;
import java.util.Map;

import org.onetwo.common.utils.Page;
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
	
	public QueryWrapper setLimited(final Integer first, final Integer size);
	
	public <T> T getRawQuery(Class<T> clazz);
	
	public QueryWrapper setQueryConfig(Map<String, Object> configs);
	
//	public DataQuery setFlushMode(FlushModeType flushMode);
	
	/*public boolean isCacheable();

	public void setCacheable(boolean cacheable);*/
	
	public void setRowMapper(RowMapper<?> rowMapper);
}