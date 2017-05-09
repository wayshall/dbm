package org.onetwo.common.db.filequery.spi;

import java.util.Optional;

import javax.sql.DataSource;

import org.onetwo.common.db.DataBase;
import org.onetwo.common.db.DbmQueryWrapper;
import org.onetwo.common.db.filequery.func.SqlFunctionDialet;
import org.onetwo.dbm.core.internal.DbmInterceptorManager;
import org.onetwo.dbm.jdbc.DbmJdbcOperations;
import org.onetwo.dbm.jdbc.mapper.RowMapperFactory;

public interface QueryProvideManager {

	DbmQueryWrapper createSQLQuery(String sqlString, Class<?> entityClass);
//	DbmQueryWrapper createQuery(String sqlString);
	FileNamedQueryFactory getFileNamedQueryManager();
	
	
	DataBase getDataBase();
	
	DataSource getDataSource();
	
//	DbmTypeMapping getSqlTypeMapping();

	RowMapperFactory getRowMapperFactory();

//	DbmSessionFactory getSessionFactory();
//	<T> T getRawManagerObject(Class<T> rawClass);

	
	
	SqlParamterPostfixFunctionRegistry getSqlParamterPostfixFunctionRegistry();
	SqlFunctionDialet getSqlFunctionDialet();
	DbmJdbcOperations getDbmJdbcOperations();
//	DataBase getDataBase();
	
	Optional<DbmInterceptorManager> getDbmInterceptorManager();
}
