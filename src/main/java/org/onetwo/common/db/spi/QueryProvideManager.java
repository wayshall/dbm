package org.onetwo.common.db.spi;

import java.util.Optional;

import javax.sql.DataSource;

import org.onetwo.common.db.filequery.func.SqlFunctionDialet;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

public interface QueryProvideManager {

	QueryWrapper createQuery(CreateQueryCmd createQueryCmd);
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
	NamedParameterJdbcOperations getDbmJdbcOperations();
//	DataBase getDataBase();

	Optional<SqlFunctionDialet> getSqlFunctionDialet();
}
