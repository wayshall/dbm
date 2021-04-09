package org.onetwo.dbm.routing.interceptor;

import org.onetwo.dbm.annotation.DbmInterceptorFilter;
import org.onetwo.dbm.annotation.DbmInterceptorFilter.InterceptorType;
import org.onetwo.dbm.core.internal.DbmInterceptorOrder;
import org.onetwo.dbm.core.internal.JdbcOperationMethodCachingService;
import org.onetwo.dbm.core.spi.DbmInterceptor;
import org.onetwo.dbm.core.spi.DbmInterceptorChain;
import org.onetwo.dbm.core.spi.DbmSessionFactory;
import org.onetwo.dbm.exception.DbmException;
import org.onetwo.dbm.jdbc.internal.SimpleArgsPreparedStatementCreator;
import org.onetwo.dbm.jdbc.method.JdbcOperationMethod;
import org.onetwo.dbm.routing.parse.RoutingTableSqlParser;
import org.springframework.core.Ordered;

/**
 * @author wayshall
 * <br/>
 */
@DbmInterceptorFilter(type=InterceptorType.JDBC)
public class ShardingTableInterceptor implements DbmInterceptor, Ordered {
	
	private RoutingTableSqlParser shardingTableSqlParser;
	private JdbcOperationMethodCachingService jdbcOperationMethodCachingService;
	private final DbmSessionFactory sessionFactory;
	
	public ShardingTableInterceptor(DbmSessionFactory sessionFactory, JdbcOperationMethodCachingService jdbcOperationMethodCachingService) {
//		this.shardingTableSqlParser = shardingTableSqlParser;
		this.sessionFactory = sessionFactory;
		this.jdbcOperationMethodCachingService = jdbcOperationMethodCachingService;
	}

	@Override
	public Object intercept(DbmInterceptorChain chain) {
		if(!shardingTableSqlParser.hasShardingConfigs()){
			return chain.invoke();
		}
		
		JdbcOperationMethod invokeMethod = jdbcOperationMethodCachingService.getJdbcOperationMethod(chain.getTargetMethod());
		Object[] args = chain.getTargetArgs();
		chain.getDatabaseOperationType();
		if (invokeMethod.getSqlParameter()!=null) {
			String sql = (String)args[invokeMethod.getSqlParameter().getParameterIndex()];
			String parsedSql = shardingTableSqlParser.parseShardingSql(sql);
			args[invokeMethod.getSqlParameter().getParameterIndex()] = parsedSql;
			
		} else if (invokeMethod.getSqlProviderParameter()!=null) {
			SimpleArgsPreparedStatementCreator sqlProvider = (SimpleArgsPreparedStatementCreator) args[invokeMethod.getSqlProviderParameter().getParameterIndex()];
			String sql = sqlProvider.getSql();
			String parsedSql = shardingTableSqlParser.parseShardingSql(sql);
			args[invokeMethod.getSqlProviderParameter().getParameterIndex()] = sqlProvider.cloneWithNewSql(parsedSql);
			
		} else {
			throw new DbmException("sql parameter not found: " + invokeMethod.getMethod().getName());
		}
		
		return chain.invoke();
	}


	@Override
	public int getOrder() {
		return DbmInterceptorOrder.before(DbmInterceptorOrder.LOG_SQL);
	}
}
