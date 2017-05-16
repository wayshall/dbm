package org.onetwo.dbm.core.spi;

import org.onetwo.dbm.jdbc.spi.DbmInterceptorChain;



public interface CachableSession extends DbmSession {

//	void putCacche(Object key, ValueWrapper value);

	Object getCaccheOrInvoke(DbmInterceptorChain chain);

}