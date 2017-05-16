package org.onetwo.dbm.jdbc.spi;

public interface DbmInterceptor {
	
	Object intercept(DbmInterceptorChain chain);

}
