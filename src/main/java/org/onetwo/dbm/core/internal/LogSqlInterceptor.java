package org.onetwo.dbm.core.internal;

import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;
import org.onetwo.common.log.JFishLoggerFactory;
import org.onetwo.common.profiling.TimeCounter;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.dbm.annotation.DbmInterceptorFilter;
import org.onetwo.dbm.annotation.DbmInterceptorFilter.InterceptorType;
import org.onetwo.dbm.core.internal.DbmThreadLocal.DbmThreadContext;
import org.onetwo.dbm.core.spi.DbmInterceptor;
import org.onetwo.dbm.core.spi.DbmInterceptorChain;
import org.onetwo.dbm.core.spi.DbmSessionFactory;
import org.onetwo.dbm.id.DbmIds;
import org.onetwo.dbm.mapping.DbmConfig;
import org.onetwo.dbm.utils.DbmUtils;
import org.slf4j.Logger;
import org.springframework.core.Ordered;

/**
 * mysql驱动本身可通过配置连接字符串打印sql，详见：
 * https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-reference-configuration-properties.html
 * jdbc:mysql://host/db?logger=com.mysql.cj.log.Log&profileSQL=true
 * 
 * @author wayshall
 * <br/>
 */
@DbmInterceptorFilter(type=InterceptorType.JDBC)
public class LogSqlInterceptor implements DbmInterceptor, Ordered {
	
	final protected static Logger logger = JFishLoggerFactory.getLogger(LogSqlInterceptor.class);
	
	final private DbmConfig dbmConfig;
	final private DbmSessionFactory sessionFactory;
	
	public LogSqlInterceptor(DbmConfig dbmConfig, DbmSessionFactory sessionFactory) {
		super();
		this.dbmConfig = dbmConfig;
		this.sessionFactory = sessionFactory;
	}

	@Override
	public Object intercept(DbmInterceptorChain chain) {
		if(!dbmConfig.isLogSql()){
			return chain.invoke();
		}
		
		Object[] args = chain.getTargetArgs();
		Pair<String, Object> sqlParams = DbmUtils.findSqlAndParams(args);
		if(sqlParams==null){
			return chain.invoke();
		}
		
		return this.invokeAndLogSql(chain, sqlParams);
	}
	
	protected Object invokeAndLogSql(DbmInterceptorChain chain, Pair<String, Object> sqlParams) {
		if(dbmConfig.isEnabledDebugContext()){
			DebugContextInterceptor.getCurrentDebugContextData().ifPresent(data->{
				data.addSqlAndParams(sqlParams);
			});
		}

		// 检查线程变量是否设置了打印sql
		Optional<DbmThreadContext> ctxOpt = DbmThreadLocal.get();
		if (ctxOpt.isPresent() && !ctxOpt.get().isLogSql()) {
			return chain.invoke();
		}

		if(logger.isTraceEnabled()){
			long txId = sessionFactory.getSession().getTransaction()!=null?sessionFactory.getSession().getTransaction().getId():DbmIds.UNKNOW_TX_ID;
			Object sqlParamValues = sqlParams.getValue();
			int maxArgSize = 50;
			if (LangUtils.size(sqlParamValues)>maxArgSize) {
				sqlParamValues = "<<Parameter Size is more than " + maxArgSize + ">>";
			} else {
				sqlParamValues = DbmUtils.formatContainerValueIfNeed(sqlParamValues);
			}
			logger.trace("tx[{}] dbm sql: {}, sql parameters: {}", txId, sqlParams.getKey(), sqlParamValues);
		}
		
		TimeCounter counter = TimeCounter.start("dbm jdbc: ");
		try {
			return chain.invoke();
		} finally {
			counter.stop(false);
			if(logger.isTraceEnabled()){
				logger.trace(counter.getMessage());
			}
		}
	}

	@Override
	public int getOrder() {
		return DbmInterceptorOrder.LOG_SQL;
	}

	protected DbmConfig getDbmConfig() {
		return dbmConfig;
	}

	protected DbmSessionFactory getSessionFactory() {
		return sessionFactory;
	}
	
	
}
