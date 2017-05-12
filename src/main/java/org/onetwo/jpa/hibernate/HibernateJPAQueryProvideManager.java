package org.onetwo.jpa.hibernate;

import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;

import org.hibernate.SQLQuery;
import org.onetwo.common.db.filequery.DbmNamedSqlFileManager;
import org.onetwo.common.db.filequery.SqlParamterPostfixFunctions;
import org.onetwo.common.db.filequery.func.SqlFunctionDialet;
import org.onetwo.common.db.spi.CreateQueryCmd;
import org.onetwo.common.db.spi.FileNamedQueryFactory;
import org.onetwo.common.db.spi.QueryProvideManager;
import org.onetwo.common.db.spi.QueryWrapper;
import org.onetwo.common.db.spi.SqlParamterPostfixFunctionRegistry;
import org.onetwo.dbm.query.DbmNamedFileQueryFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * @author wayshall
 * <br/>
 */
public class HibernateJPAQueryProvideManager implements QueryProvideManager {

	private DataSource dataSource;
	private NamedParameterJdbcOperations jdbcOperations;
	private SqlParamterPostfixFunctionRegistry sqlParamterPostfixFunctionRegistry = new SqlParamterPostfixFunctions();
	private DbmNamedFileQueryFactory dbmNamedFileQueryFactory;
	
	@PersistenceContext
	private EntityManager entityManager;
	
	
	public HibernateJPAQueryProvideManager(DataSource dataSource) {
		super();
		this.dataSource = dataSource;
		this.jdbcOperations = new NamedParameterJdbcTemplate(dataSource);
		DbmNamedSqlFileManager sqlFileManager = DbmNamedSqlFileManager.createNamedSqlFileManager(true);
		dbmNamedFileQueryFactory = new DbmNamedFileQueryFactory(sqlFileManager);
	}

	@Override
	public QueryWrapper createQuery(CreateQueryCmd createQueryCmd) {
		if(createQueryCmd.isNativeSql()){
			SQLQuery sqlQuery = entityManager.createNativeQuery(createQueryCmd.getSql()).unwrap(SQLQuery.class);
			sqlQuery.setResultTransformer(new HibernateRowToBeanTransformer(createQueryCmd.getMappedClass()));
			HibernateDbmQueryWrapper wrapper = new HibernateDbmQueryWrapper(sqlQuery);
			return wrapper;
		}else{
			throw new UnsupportedOperationException("Unsupported not native sql");
		}
	}

	@Override
	public FileNamedQueryFactory getFileNamedQueryManager() {
		return dbmNamedFileQueryFactory;
	}

	@Override
	public NamedParameterJdbcOperations getJdbcOperations() {
		return jdbcOperations;
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
	public Optional<SqlFunctionDialet> getSqlFunctionDialet() {
		return Optional.empty();
	}

}
