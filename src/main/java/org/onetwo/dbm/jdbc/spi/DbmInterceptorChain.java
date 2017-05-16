package org.onetwo.dbm.jdbc.spi;

import java.lang.reflect.Method;
import java.util.Optional;

import org.onetwo.dbm.annotation.DbmInterceptorFilter.InterceptorType;
import org.onetwo.dbm.jdbc.spi.DbmJdbcOperationType.DatabaseOperationType;

public interface DbmInterceptorChain {
	
	InterceptorType getType();
	
	Object invoke();

	Object getResult();
	
	Throwable getThrowable();

	Object getTargetObject();

	Method getTargetMethod();

	Object[] getTargetArgs();

	Optional<DbmJdbcOperationType> getJdbcOperationType();
	Optional<DatabaseOperationType> getDatabaseOperationType();

	DbmInterceptorChain addInterceptorToHead(DbmInterceptor...interceptors);
	
	DbmInterceptorChain addInterceptorToTail(DbmInterceptor...interceptors);
	
	DbmInterceptorChain addInterceptor(DbmInterceptor...interceptors);
}