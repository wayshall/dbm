package org.onetwo.jpa.hibernate;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;

import org.hibernate.SQLQuery;
import org.onetwo.common.db.ParsedSqlContext;
import org.onetwo.common.db.dquery.DynamicMethod;
import org.onetwo.common.db.dquery.NamedQueryInvokeContext;
import org.onetwo.common.db.filequery.DbmNamedSqlFileManager;
import org.onetwo.common.db.filequery.SqlParamterPostfixFunctions;
import org.onetwo.common.db.filequery.func.SqlFunctionDialet;
import org.onetwo.common.db.spi.CreateQueryCmd;
import org.onetwo.common.db.spi.FileNamedQueryFactory;
import org.onetwo.common.db.spi.QueryProvideManager;
import org.onetwo.common.db.spi.QueryWrapper;
import org.onetwo.common.db.spi.SqlParamterPostfixFunctionRegistry;
import org.onetwo.common.utils.Page;
import org.onetwo.dbm.annotation.DbmResultMapping;
import org.onetwo.dbm.core.spi.DbmInterceptor;
import org.onetwo.dbm.core.spi.DbmInterceptorChain;
import org.onetwo.dbm.query.DbmFileQueryWrapperImpl;
import org.onetwo.dbm.query.DbmNamedFileQueryFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterUtils;

import com.google.common.collect.ImmutableList;

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
	
	private Collection<DbmInterceptor> interceptors = ImmutableList.of(new StripNullDbmInterceptor());
	
	
	public HibernateJPAQueryProvideManager(DataSource dataSource) {
		super();
		this.dataSource = dataSource;
		this.jdbcOperations = new NamedParameterJdbcTemplate(dataSource);
		DbmNamedSqlFileManager sqlFileManager = DbmNamedSqlFileManager.createNamedSqlFileManager(true);
		dbmNamedFileQueryFactory = new HibernateNamedFileQueryFactory(sqlFileManager);
	}

	@Override
	public Collection<DbmInterceptor> getRepositoryInterceptors() {
		return interceptors;
	}

	@Override
	public QueryWrapper createQuery(CreateQueryCmd createQueryCmd) {
		if(createQueryCmd.isNativeSql()){
			SQLQuery sqlQuery = entityManager.createNativeQuery(createQueryCmd.getSql()).unwrap(SQLQuery.class);
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
	
	static class StripNullDbmInterceptor implements DbmInterceptor {

		@Override
		public Object intercept(DbmInterceptorChain chain) {
			Object result = chain.invoke();
			return stripNull(result);
		}

		/****
		 * 主要是解决hibernate下，TupleSubsetResultTransformer无法避免把返回null值不添加到结果集的问题
		 * @author wayshall
		 * @param datas
		 */
		private <T> T stripNull(T datas){
			if(datas instanceof Collection){
				((Collection<?>)datas).removeIf(Objects::isNull);
			}else if(datas instanceof Page){
				Page<?> page = (Page<?>) datas;
				page.getResult().removeIf(Objects::isNull);
			}
			return datas;
		}
	}

	static class HibernateNamedFileQueryFactory extends DbmNamedFileQueryFactory {

		public HibernateNamedFileQueryFactory(DbmNamedSqlFileManager sqlFileManager) {
			super(sqlFileManager);
		}

		@Override
		protected QueryWrapper newQueryWrapperInstance(boolean count, NamedQueryInvokeContext invokeContext) {
			return new HiberanteFileQueryWrapperImpl(invokeContext, count);
		}
		
	}
	
	static class HiberanteFileQueryWrapperImpl extends DbmFileQueryWrapperImpl {

		public HiberanteFileQueryWrapperImpl(NamedQueryInvokeContext invokeContext, boolean count) {
			super(invokeContext, count);
		}
		
		protected QueryWrapper createDataQueryIfNecessarry(){
			if(dataQuery!=null){
				return dataQuery;
			}

			ParsedSqlContext sqlAndValues = createParsedSqlContext();
			QueryWrapper dataQuery = null;
			if(sqlAndValues.isListValue()){
				CreateQueryCmd createQueryCmd = new CreateQueryCmd(sqlAndValues.getParsedSql(), resultClass, info.isNativeSql());
				dataQuery = createDataQuery(createQueryCmd);
				doIndexParameters(dataQuery, sqlAndValues.asList());
				
			}else{
				Map<String, Object> params = processNamedParameters(sqlAndValues);
				MapSqlParameterSource mps = new MapSqlParameterSource(params);
				
				String sqlToUse = NamedParameterUtils.substituteNamedParameters(sqlAndValues.getParsedSql(), mps);
				Object[] arrayParams = NamedParameterUtils.buildValueArray(NamedParameterUtils.parseSqlStatement(sqlAndValues.getParsedSql()), mps, null);

				CreateQueryCmd createQueryCmd = new CreateQueryCmd(sqlToUse, resultClass, info.isNativeSql());
				dataQuery = createDataQuery(createQueryCmd);
				doIndexParameters(dataQuery, Arrays.asList(arrayParams));
				setLimitResult(dataQuery);
			}

			this.dataQuery = dataQuery;
			return dataQuery;
		}

		
		protected void doIndexParameters(QueryWrapper dataQuery, List<Object> values){
			/*int position = 0;
			for(Object value : values){
				dataQuery.setParameter(position++, value);
			}*/
			int position = 0;
			for(Object in : values){
				if (in instanceof Collection ) {
					Collection<?> entries = (Collection<?>) in;
					for (Object entry : entries) {
						if (entry instanceof Object[]) {
							Object[] valueArray = ((Object[])entry);
							for (Object argValue : valueArray) {
								dataQuery.setParameter(position++, argValue);
							}
						}
						else {
							dataQuery.setParameter(position++, entry);
						}
					}
				}
				else {
					dataQuery.setParameter(position++, in);
				}
			}
			setLimitResult(dataQuery);
		}
		
		protected QueryWrapper createDataQuery(CreateQueryCmd createQueryCmd){
			QueryWrapper dataQuery = this.queryProvideManager.createQuery(createQueryCmd);
			SQLQuery sqlQuery = dataQuery.unwarp(SQLQuery.class);
			DynamicMethod dmethod = invokeContext.getDynamicMethod();
			if(dmethod.isAnnotationPresent(DbmResultMapping.class)){
				DbmResultMapping dbmResultMapping = dmethod.getMethod().getAnnotation(DbmResultMapping.class);
				HibernateNestedBeanTransformer<?> transformer = new HibernateNestedBeanTransformer<>(dmethod.getComponentClass(), dbmResultMapping);
				sqlQuery.setResultTransformer(transformer);
			}else{
				sqlQuery.setResultTransformer(new HibernateRowToBeanTransformer(createQueryCmd.getMappedClass()));
			}
			return dataQuery;
		}
	}
}
