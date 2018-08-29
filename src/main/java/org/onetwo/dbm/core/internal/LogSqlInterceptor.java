package org.onetwo.dbm.core.internal;

import org.apache.commons.lang3.tuple.Pair;
import org.onetwo.common.log.JFishLoggerFactory;
import org.onetwo.common.profiling.TimeCounter;
import org.onetwo.dbm.annotation.DbmInterceptorFilter;
import org.onetwo.dbm.annotation.DbmInterceptorFilter.InterceptorType;
import org.onetwo.dbm.core.spi.DbmInterceptor;
import org.onetwo.dbm.core.spi.DbmInterceptorChain;
import org.onetwo.dbm.mapping.DbmConfig;
import org.onetwo.dbm.utils.DbmUtils;
import org.slf4j.Logger;
import org.springframework.core.Ordered;

/**
 * @author wayshall
 * <br/>
 */
@DbmInterceptorFilter(type=InterceptorType.JDBC)
public class LogSqlInterceptor implements DbmInterceptor, Ordered {
	
	final private static Logger logger = JFishLoggerFactory.getLogger(LogSqlInterceptor.class);
	
	final private DbmConfig dbmConfig;
	
	public LogSqlInterceptor(DbmConfig dbmConfig) {
		super();
		this.dbmConfig = dbmConfig;
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
		
		if(dbmConfig.isEnabledDebugContext()){
			DebugContextInterceptor.getCurrentDebugContextData().ifPresent(data->{
				data.addSqlAndParams(sqlParams);
			});
		}
		if(logger.isTraceEnabled()){
			logger.trace("dbm sql: {}, sql parameters: {}", sqlParams.getKey(), sqlParams.getValue());
		}
		
		TimeCounter counter = TimeCounter.start("dbm jdbc: ");
		try {
			return chain.invoke();
		}finally{
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
	
	
}
