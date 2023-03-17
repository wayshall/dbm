package org.onetwo.jpa.hibernate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.hibernate.LockOptions;
import org.hibernate.SQLQuery;
import org.onetwo.common.db.AbstractQueryWrapper;
import org.onetwo.common.db.spi.QueryWrapper;
import org.onetwo.common.reflect.ReflectUtils;
import org.onetwo.dbm.dialet.DBDialect.LockInfo;
import org.onetwo.dbm.exception.DbmException;
import org.onetwo.dbm.utils.DbmLock;
import org.springframework.jdbc.core.RowMapper;

/**
 * @author wayshall
 * <br/>
 */
@SuppressWarnings({ "unchecked" })
public class HibernateDbmQueryWrapper extends AbstractQueryWrapper implements QueryWrapper {
	
	final private SQLQuery sqlQuery;

	public HibernateDbmQueryWrapper(SQLQuery sqlQuery) {
		super();
		this.sqlQuery = sqlQuery;
	}

	public QueryWrapper setLockInfo(LockInfo lockInfo){
		if(lockInfo.getLock()==DbmLock.PESSIMISTIC_WRITE){
			sqlQuery.setLockOptions(LockOptions.UPGRADE);
		}else if(lockInfo.getLock()==DbmLock.PESSIMISTIC_READ){
			sqlQuery.setLockOptions(LockOptions.READ);
		}
		return this;
	}
	
	@Override
	public int executeUpdate() {
		return sqlQuery.executeUpdate();
	}

	@Override
	public <T> List<T> getResultList() {
		return sqlQuery.list();
	}

	@Override
	public <T> T getSingleResult() {
		return (T)sqlQuery.uniqueResult();
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
		try {
			sqlQuery.setParameter(position, value);
		} catch (IllegalArgumentException e) {
			if (e.getLocalizedMessage().contains("Could not locate ordinal parameter")) {
				throw new DbmException(e.getMessage() + ". try to set hibernate.query.sql.jdbc_style_params_base=true", e);
			} else {
				throw e;
			}
		}
		return this;
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
	public void setRowMapper(RowMapper<?> rowMapper) {
		throw new UnsupportedOperationException();
	}
	@Override
	public <T> T unwarp(Class<T> clazz) {
		return clazz.cast(sqlQuery);
	}

	@Override
	public Map<?, Object> getParameters() {
		try {
			return (Map<?, Object>)ReflectUtils.invokeMethod("getNamedParams", sqlQuery);
		} catch (Exception e) {
			logger.warn("getNamedParams from hibernate sql query error!");
			return Collections.EMPTY_MAP;
		}
	}

}
