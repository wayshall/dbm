package org.onetwo.dbm.core.internal;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;

import org.onetwo.dbm.exception.DbmException;
import org.onetwo.dbm.jdbc.method.JdbcOperationMethod;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * @author weishao zeng
 * <br/>
 */
public class JdbcMethodCacheService {

//	final protected static Cache<Method, JdbcOperationMethod> JDBC_METHOD_CACHES = CacheBuilder.newBuilder().<Method, JdbcOperationMethod>build();
	final private Cache<Method, JdbcOperationMethod> methodCache = CacheBuilder.newBuilder().<Method, JdbcOperationMethod>build();
	
	public JdbcOperationMethod getJdbcMethod(Method method) {
		JdbcOperationMethod invokeMethod;
		try {
			invokeMethod = methodCache.get(method, () -> {
				return createMethod(method);
			});
		} catch (ExecutionException e) {
			throw new DbmException("find jdbc method from cache error: " + e.getMessage(), e);
		}
		
		return invokeMethod;
	}

	protected JdbcOperationMethod createMethod(Method method) {
		return new JdbcOperationMethod(method);
	}
}
