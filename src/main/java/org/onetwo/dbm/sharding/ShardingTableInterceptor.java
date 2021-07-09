package org.onetwo.dbm.sharding;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;
import org.onetwo.common.db.DruidUtils;
import org.onetwo.common.db.generator.meta.TableMeta;
import org.onetwo.common.log.JFishLoggerFactory;
import org.onetwo.dbm.annotation.DbmInterceptorFilter;
import org.onetwo.dbm.annotation.DbmInterceptorFilter.InterceptorType;
import org.onetwo.dbm.core.internal.DbmInterceptorOrder;
import org.onetwo.dbm.core.internal.JdbcMethodCacheService;
import org.onetwo.dbm.core.spi.DbmInterceptor;
import org.onetwo.dbm.core.spi.DbmInterceptorChain;
import org.onetwo.dbm.core.spi.DbmSessionFactory;
import org.onetwo.dbm.druid.DbmMySqlLexer;
import org.onetwo.dbm.exception.DbmException;
import org.onetwo.dbm.jdbc.method.JdbcOperationMethod;
import org.onetwo.dbm.mapping.DbmConfig;
import org.onetwo.dbm.utils.DbmUtils;
import org.slf4j.Logger;
import org.springframework.core.Ordered;
import org.springframework.util.Assert;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.util.JdbcConstants;

/**
 * @author weishao zeng
 * <br/>
 */
@DbmInterceptorFilter(type=InterceptorType.JDBC)
public class ShardingTableInterceptor implements DbmInterceptor, Ordered {

	final protected static Logger logger = JFishLoggerFactory.getLogger(ShardingTableInterceptor.class);
	
	final private DbmConfig dbmConfig;
	final private DbmSessionFactory sessionFactory;
	private JdbcMethodCacheService jdbcMethodCacheService;

	public ShardingTableInterceptor(JdbcMethodCacheService JdbcMethodCacheService, DbmConfig dbmConfig, DbmSessionFactory sessionFactory) {
		Assert.notNull(dbmConfig, "dbmConfig can not be null!");
		Assert.notNull(sessionFactory, "sessionFactory can not be null!");
		Assert.notNull(JdbcMethodCacheService, "JdbcMethodCacheService can not be null!");
		this.dbmConfig = dbmConfig;
		this.sessionFactory = sessionFactory;
		this.jdbcMethodCacheService = JdbcMethodCacheService;
	}
	
	private void checkShardingTable(String tableName) {
		Optional<TableMeta> tableOpt = sessionFactory.getDatabaseMetaDialet().findTableMeta(tableName);
		if (!tableOpt.isPresent()) {
			throw new DbmException("sharding table not found: " + tableName);
		}
	}
	
	@Override
	public Object intercept(DbmInterceptorChain chain) {
		if(dbmConfig.getShardingTables().isEmpty()){
			return chain.invoke();
		}
		
		JdbcOperationMethod invokeMethod = jdbcMethodCacheService.getJdbcMethod(chain.getTargetMethod());
		
		Pair<String, Object> sqlParams = DbmUtils.findSqlAndParams(invokeMethod, chain.getTargetArgs());
		
		DbmMySqlLexer lexer = new DbmMySqlLexer(sqlParams.getKey());
		List<SQLStatement> stmtList = DruidUtils.parseStatements(lexer, getDbType());
		
		return null;
	}
	
	private DbType getDbType() {
		return JdbcConstants.MYSQL;
	}

	@Override
	public int getOrder() {
		return DbmInterceptorOrder.before(DbmInterceptorOrder.SESSION_CACHE);
	}

}
