package org.onetwo.jpa.hibernate;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;

import org.hibernate.SQLQuery;
import org.onetwo.common.db.DbmQueryWrapper;
import org.onetwo.common.db.filequery.DbmNamedSqlFileManager;
import org.onetwo.common.db.filequery.SqlParamterPostfixFunctions;
import org.onetwo.common.db.filequery.func.SqlFunctionDialet;
import org.onetwo.common.db.filequery.spi.CreateQueryCmd;
import org.onetwo.common.db.filequery.spi.FileNamedQueryFactory;
import org.onetwo.common.db.filequery.spi.QueryProvideManager;
import org.onetwo.common.db.filequery.spi.SqlParamterPostfixFunctionRegistry;
import org.onetwo.dbm.core.internal.DbmInterceptorManager;
import org.onetwo.dbm.jdbc.DbmJdbcOperations;
import org.onetwo.dbm.jdbc.DbmJdbcTemplate;
import org.onetwo.dbm.jdbc.mapper.RowMapperFactory;
import org.onetwo.dbm.query.DbmNamedFileQueryFactory;

/**
 * @author wayshall
 * <br/>
 */
public class HibernateQueryProvideManager implements QueryProvideManager {

	private DataSource dataSource;
	private DbmJdbcOperations dbmJdbcOperations;
	private SqlParamterPostfixFunctionRegistry sqlParamterPostfixFunctionRegistry = new SqlParamterPostfixFunctions();
	private DbmNamedFileQueryFactory dbmNamedFileQueryFactory;
	
	@PersistenceContext
	private EntityManager entityManager;
	
	
	public HibernateQueryProvideManager(DataSource dataSource) {
		super();
		this.dataSource = dataSource;
		
		this.dbmJdbcOperations = new DbmJdbcTemplate(dataSource);
		DbmNamedSqlFileManager sqlFileManager = DbmNamedSqlFileManager.createNamedSqlFileManager(true);
		dbmNamedFileQueryFactory = new DbmNamedFileQueryFactory(sqlFileManager);
	}

	@Override
	public DbmQueryWrapper createQuery(CreateQueryCmd createQueryCmd) {
		if(createQueryCmd.isNativeSql()){
			SQLQuery sqlQuery = entityManager.createNativeQuery(createQueryCmd.getSql()).unwrap(SQLQuery.class);
			sqlQuery.setResultTransformer(new RowToBeanTransformer(createQueryCmd.getMappedClass()));
			List<?> datas = sqlQuery.list();
		}else{
			throw new UnsupportedOperationException("Unsupported not native sql");
		}
		return null;
	}

	@Override
	public FileNamedQueryFactory getFileNamedQueryManager() {
		return dbmNamedFileQueryFactory;
	}

	@Override
	public DataSource getDataSource() {
		return dataSource;
	}

	@Override
	public SqlParamterPostfixFunctionRegistry getSqlParamterPostfixFunctionRegistry() {
		return sqlParamterPostfixFunctionRegistry;
	}

	@Override
	public DbmJdbcOperations getDbmJdbcOperations() {
		return dbmJdbcOperations;
	}

	@Override
	public Optional<RowMapperFactory> getRowMapperFactory() {
		return Optional.empty();
	}

	@Override
	public Optional<SqlFunctionDialet> getSqlFunctionDialet() {
		return Optional.empty();
	}

	@Override
	public Optional<DbmInterceptorManager> getDbmInterceptorManager() {
		return Optional.empty();
	}
	
	

}
