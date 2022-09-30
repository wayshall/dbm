package org.onetwo.dbm.query;

import java.util.List;
import java.util.Map;

import org.onetwo.dbm.dialet.DBDialect.LockInfo;
import org.springframework.jdbc.core.RowMapper;

public interface DbmQuery {

	DbmQuery setParameter(Integer index, Object value);

	DbmQuery setParameter(String name, Object value);

	<T> T getSingleResult();

	<T> List<T> getResultList();

	/***
	 * 
	 * @author weishao zeng
	 * @param firstResult from 0
	 * @return
	 */
	DbmQuery setFirstResult(int firstResult);

	DbmQuery setMaxResults(int maxResults);
	
	DbmQuery setResultClass(Class<?> resultClass);
	
	DbmQuery setParameters(Map<String, Object> params);
	
	DbmQuery setParameters(List<?> params);
	
	Map<String, Object> getParameters();
	
	int executeUpdate();
	
	void setRowMapper(RowMapper<?> rowMapper);
	void setQueryAttributes(Map<Object, Object> params);
	
	void setLockInfo(LockInfo lockInfo);
}