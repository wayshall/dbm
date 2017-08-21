package org.onetwo.dbm.core.internal;

import java.util.Map;
import java.util.Optional;

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
		Optional<String> sqlOpt = findSql(args);
		if(!sqlOpt.isPresent()){
			return chain.invoke();
		}
		Optional<Object> sqlParamsOpt = findSqlParams(args);
		if(dbmConfig.isLogSql() && logger.isInfoEnabled()){
			logger.info("dbm sql: {}, sql parameters: {}", sqlOpt.get(), sqlParamsOpt.orElse("<NULL>"));
		}
		return chain.invoke();
	}
	
	private Optional<String> findSql(Object[] args){
		for (int i = 0; i < args.length; i++) {
			Object arg = args[i];
			if(arg instanceof String){
				return Optional.ofNullable((String)arg);
			}else if(arg instanceof SqlProvider){
				return Optional.ofNullable(((SqlProvider)arg).getSql());
			}
		}
		return Optional.empty();
	}
	
	private Optional<Object> findSqlParams(Object[] args){
		for (int i = 0; i < args.length; i++) {
			Object arg = args[i];
			if(arg==null){
				continue;
			}
			if(arg instanceof Map){
				return Optional.of(arg);
			}else if(arg.getClass().isArray()){
				return Optional.of(CUtils.tolist(arg, false));
			}else if(arg instanceof SqlProvider){
				return Optional.ofNullable(((SqlParametersProvider)arg).getSqlParameterList());
			}
		}
		return Optional.empty();
	}
	
	

}
