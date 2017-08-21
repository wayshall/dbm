package org.onetwo.dbm.core.internal;

import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.onetwo.common.log.JFishLoggerFactory;
import org.onetwo.common.utils.CUtils;
import org.onetwo.dbm.annotation.DbmInterceptorFilter;
import org.onetwo.dbm.annotation.DbmInterceptorFilter.InterceptorType;
import org.onetwo.dbm.jdbc.spi.DbmInterceptor;
import org.onetwo.dbm.jdbc.spi.DbmInterceptorChain;
import org.onetwo.dbm.jdbc.spi.SqlParametersProvider;
import org.onetwo.dbm.mapping.DbmConfig;
import org.slf4j.Logger;
import org.springframework.jdbc.core.SqlProvider;

/**
 * @author wayshall
 * <br/>
 */
@DbmInterceptorFilter(type=InterceptorType.JDBC)
public class LogSqlInterceptor implements DbmInterceptor {
	
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
		Pair<String, Object> sqlParams = findSqlAndParams(args);
		if(sqlParams==null){
			return chain.invoke();
		}
		if(logger.isInfoEnabled()){
			logger.info("dbm sql: {}, sql parameters: {}", sqlParams.getKey(), sqlParams.getValue());
		}
		return chain.invoke();
	}
	
	private Pair<String, Object> findSqlAndParams(Object[] args){
		String sql = null;
		Object params = null;
		for (int i = 0; i < args.length; i++) {
			Object arg = args[i];
			if(arg==null){
				continue;
			}
			if(arg instanceof String){
				sql = (String)arg;
			}else if(arg instanceof Map){
				params = arg;
			}else if(arg.getClass().isArray()){
				params = CUtils.tolist(arg, false);
			}else{
				//if arg is SimpleArgsPreparedStatementCreator
				if(arg instanceof SqlProvider){
					sql = ((SqlProvider)arg).getSql();
				}
				if(arg instanceof SqlParametersProvider){
					params = ((SqlParametersProvider)arg).getSqlParameterList();
				}
			}
			
		}
		if(sql==null){
			return null;
		}
		return Pair.of(sql, params);
	}
	
}
