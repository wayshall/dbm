package org.onetwo.dbm.core.internal;

import java.util.Optional;

import org.onetwo.dbm.core.spi.CachableSession;
import org.onetwo.dbm.core.spi.DbmInterceptor;
import org.onetwo.dbm.core.spi.DbmInterceptorChain;
import org.onetwo.dbm.core.spi.DbmSession;
import org.onetwo.dbm.core.spi.DbmSessionFactory;
import org.onetwo.dbm.jdbc.spi.DbmJdbcOperationType.DatabaseOperationType;
import org.springframework.core.Ordered;

public class SessionCacheInterceptor implements DbmInterceptor, Ordered {
	
	private DbmSessionFactory sessionFactory;

	public SessionCacheInterceptor(DbmSessionFactory sessionFactory) {
		super();
		this.sessionFactory = sessionFactory;
	}

	@Override
	public Object intercept(DbmInterceptorChain chain) {
		Optional<DbmSession> sessionOpt = sessionFactory.getCurrentSession();
		if(!sessionOpt.isPresent() || !CachableSession.class.isInstance(sessionOpt.get())){
			return chain.invoke();
		}
		
		CachableSession session = (CachableSession)sessionOpt.get();
		Optional<DatabaseOperationType> operationOpt = chain.getDatabaseOperationType();
		if(operationOpt.isPresent()){
			if(operationOpt.get()==DatabaseOperationType.QUERY){
				return session.getCaccheOrInvoke(chain);
			}
			chain.invoke();
			session.flush();
			return chain.getResult();
		}
		return chain.invoke();
	}

	@Override
	public int getOrder() {
		return DbmInterceptorOrder.SESSION_CACHE;
	}

}
