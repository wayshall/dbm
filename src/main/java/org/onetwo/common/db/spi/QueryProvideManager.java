package org.onetwo.common.db.spi;

import java.util.Collection;
import java.util.Optional;

import javax.sql.DataSource;

import org.onetwo.common.db.filequery.func.SqlFunctionDialet;
import org.onetwo.dbm.core.spi.DbmInterceptor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

public interface QueryProvideManager {

	QueryWrapper createQuery(CreateQueryCmd createQueryCmd);
//	DbmQueryWrapper createQuery(String sqlString);
	FileNamedQueryFactory getFileNamedQueryManager();
	
	DataSource getDataSource();
	
	SqlParamterPostfixFunctionRegistry getSqlParamterPostfixFunctionRegistry();
	
	/***
	 * only for batch operation
	 * @author wayshall
	 * @return
	 */
	NamedParameterJdbcOperations getJdbcOperations();
//	DataBase getDataBase();

	Optional<SqlFunctionDialet> getSqlFunctionDialet();
	
	Collection<DbmInterceptor> getRepositoryInterceptors();
}
