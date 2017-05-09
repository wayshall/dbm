package org.onetwo.jpa.hibernate;

import java.util.List;
import java.util.Map;

import org.hibernate.SQLQuery;
import org.onetwo.common.db.AbstractQueryWrapper;
import org.onetwo.common.db.spi.QueryWrapper;
import org.springframework.jdbc.core.RowMapper;

/**
 * @author wayshall
 * <br/>
 */
public class HibernateDbmQueryWrapper<E> extends AbstractQueryWrapper implements QueryWrapper {
	
	private SQLQuery<E> sqlQuery;

	@Override
	public int executeUpdate() {
		return sqlQuery.executeUpdate();
	}

	@Override
	public <T> List<T> getResultList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T getSingleResult() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public QueryWrapper setFirstResult(int startPosition) {
		sqlQuery.setFirstResult(startPosition);
		return this;
	}

	@Override
	public QueryWrapper setMaxResults(int maxResult) {
		sqlQuery.setMaxResults(maxResult);
		return this;
	}

	@Override
	public QueryWrapper setParameter(int position, Object value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public QueryWrapper setParameter(String name, Object value) {
		sqlQuery.setParameter(name, value);
		return this;
	}


	@Override
	public QueryWrapper setLimited(Integer first, Integer size) {
		sqlQuery.setFirstResult(first);
		sqlQuery.setMaxResults(size);
		return this;
	}

	@Override
	public <T> T getRawQuery(Class<T> clazz) {
		return clazz.cast(sqlQuery);
	}

	@Override
	public QueryWrapper setQueryConfig(Map<String, Object> configs) {
		logger.info("ingore set query config");
		return this;
	}

	@Override
	public void setRowMapper(RowMapper<?> rowMapper) {
		throw new UnsupportedOperationException();
	}
	
	

}
