package org.onetwo.dbm.core.spi;

import org.onetwo.common.db.spi.BaseEntityManager;
import org.onetwo.dbm.core.internal.DbmInterceptorManager;
import org.onetwo.dbm.jdbc.mapper.RowMapperFactory;

public interface DbmEntityManager extends BaseEntityManager {
	
	
//	public <T> Page<T> findPageByQName(String queryName, RowMapper<T> rowMapper, Page<T> page, Object... params);
	
	DbmSessionImplementor getCurrentSession();
	

	RowMapperFactory getRowMapperFactory();
	DbmInterceptorManager getDbmInterceptorManager();
	
}
