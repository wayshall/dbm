package org.onetwo.common.db.filequery.spi;

import java.util.Optional;

import javax.sql.DataSource;

import org.onetwo.common.db.DbmQueryWrapper;
import org.onetwo.common.db.filequery.func.SqlFunctionDialet;
import org.onetwo.dbm.core.internal.DbmInterceptorManager;
import org.onetwo.dbm.jdbc.DbmJdbcOperations;
import org.onetwo.dbm.jdbc.mapper.RowMapperFactory;

public interface QueryProvideManager {

	DbmQueryWrapper createQuery(CreateQueryCmd createQueryCmd);
//	DbmQueryWrapper createQuery(String sqlString);
	FileNamedQueryFactory getFileNamedQueryManager();
	
	
//	DataBase getDataBase();
	
	DataSource getDataSource();
	
//	DbmTypeMapping getSqlTypeMapping();


//	DbmSessionFactory getSessionFactory();
//	<T> T getRawManagerObject(Class<T> rawClass);

	
	
	SqlParamterPostfixFunctionRegistry getSqlParamterPostfixFunctionRegistry();
	
	/***
	 * only for batch operation
	 * @author wayshall
	 * @return
	 */
	DbmJdbcOperations getDbmJdbcOperations();
//	DataBase getDataBase();

	Optional<RowMapperFactory> getRowMapperFactory();
	Optional<SqlFunctionDialet> getSqlFunctionDialet();
	Optional<DbmInterceptorManager> getDbmInterceptorManager();
}
