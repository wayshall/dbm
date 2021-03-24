package org.onetwo.dbm.core.internal;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.tuple.Pair;
import org.onetwo.dbm.annotation.DbmInterceptorFilter;
import org.onetwo.dbm.annotation.DbmInterceptorFilter.InterceptorType;
import org.onetwo.dbm.core.spi.DbmInterceptor;
import org.onetwo.dbm.core.spi.DbmInterceptorChain;
import org.onetwo.dbm.core.spi.DbmSessionFactory;
import org.onetwo.dbm.exception.DbmException;
import org.onetwo.dbm.jdbc.method.JdbcOperationMethod;
import org.onetwo.dbm.mapping.DbmConfig;
import org.onetwo.dbm.utils.DbmUtils;
import org.springframework.core.Ordered;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * mysql驱动本身可通过配置连接字符串打印sql，详见：
 * https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-reference-configuration-properties.html
 * jdbc:mysql://host/db?logger=com.mysql.cj.log.Log&profileSQL=true
 * 
 * @author wayshall
 * <br/>
 */
@DbmInterceptorFilter(type=InterceptorType.JDBC)
public class LogSqlByAnnotationInterceptor extends LogSqlInterceptor implements DbmInterceptor, Ordered {
	
	final protected static Cache<Method, JdbcOperationMethod> JDBC_METHOD_CACHES = CacheBuilder.newBuilder().<Method, JdbcOperationMethod>build();
	final private Cache<Method, JdbcOperationMethod> methodCache = JDBC_METHOD_CACHES;
	
	public LogSqlByAnnotationInterceptor(DbmConfig dbmConfig, DbmSessionFactory sessionFactory) {
		super(dbmConfig, sessionFactory);
	}

	@Override
	public Object intercept(DbmInterceptorChain chain) {
		DbmConfig dbmConfig = getDbmConfig();
		if(!dbmConfig.isLogSql()){
			return chain.invoke();
		}
		
		JdbcOperationMethod invokeMethod;
		try {
			invokeMethod = methodCache.get(chain.getTargetMethod(), () -> {
				return createMethod(chain.getTargetMethod());
			});
		} catch (ExecutionException e) {
			throw new DbmException("find jdbc method from cache error: " + e.getMessage(), e);
		}
		
		Pair<String, Object> sqlParams = DbmUtils.findSqlAndParams(invokeMethod, chain.getTargetArgs());
		
		return invokeAndLogSql(chain, sqlParams);
	}

	protected JdbcOperationMethod createMethod(Method method) {
		return new JdbcOperationMethod(method);
	}

}
