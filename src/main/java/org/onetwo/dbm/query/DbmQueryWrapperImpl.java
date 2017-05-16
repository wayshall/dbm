package org.onetwo.dbm.query;

import java.util.List;
import java.util.Map;

import org.onetwo.common.db.AbstractQueryWrapper;
import org.onetwo.common.db.spi.QueryWrapper;
import org.onetwo.common.utils.LangUtils;
import org.springframework.jdbc.core.RowMapper;

public class DbmQueryWrapperImpl extends AbstractQueryWrapper {
	
	private DbmQuery dbmQuery;
	
	public DbmQueryWrapperImpl(DbmQuery jfishQuery) {
		super();
		this.dbmQuery = jfishQuery;
	}

	@Override
	public int executeUpdate() {
		return dbmQuery.executeUpdate();
	}

	@Override
	public <T> List<T> getResultList() {
		return dbmQuery.getResultList();
	}

	@Override
	public <T> T getSingleResult() {
		return dbmQuery.getSingleResult();
	}

	@Override
	public QueryWrapper setFirstResult(int startPosition) {
		dbmQuery.setFirstResult(startPosition);
		return this;
	}

	@Override
	public QueryWrapper setMaxResults(int maxResult) {
		dbmQuery.setMaxResults(maxResult);
		return this;
	}

	@Override
	public QueryWrapper setParameter(int position, Object value) {
		dbmQuery.setParameter(position, value);
		return this;
	}

	@Override
	public QueryWrapper setParameter(String name, Object value) {
		dbmQuery.setParameter(name, value);
		return this;
	}

	@Override
	public QueryWrapper setParameters(Map<String, Object> params) {
		dbmQuery.setParameters(params);
		return this;
	}

	@Override
	public QueryWrapper setParameters(List<Object> params) {
		dbmQuery.setParameters(params);
		return this;
	}

	@Override
	public QueryWrapper setParameters(Object[] params) {
		dbmQuery.setParameters(LangUtils.asList(params));
		return this;
	}


	@Override
	public QueryWrapper setLimited(Integer first, Integer size) {
		if (first >= 0) {
			dbmQuery.setFirstResult(first);
		}
		if (size >= 1) {
			dbmQuery.setMaxResults(size);
		}
		return this;
	}

	@Override
	public <T> T getRawQuery(Class<T> clazz) {
		return clazz.cast(dbmQuery);
	}

	public DbmQuery getJfishQuery() {
		return dbmQuery;
	}

	@Override
	public void setRowMapper(RowMapper<?> rowMapper) {
		this.dbmQuery.setRowMapper(rowMapper);
	}

	@Override
	public <T> T unwarp(Class<T> clazz) {
		return clazz.cast(dbmQuery);
	}

}
