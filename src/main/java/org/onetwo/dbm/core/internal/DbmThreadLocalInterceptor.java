package org.onetwo.dbm.core.internal;

import org.onetwo.dbm.core.spi.DbmInterceptor;
import org.onetwo.dbm.core.spi.DbmInterceptorChain;
import org.onetwo.dbm.core.spi.DbmSessionFactory;
import org.springframework.core.Ordered;

public class DbmThreadLocalInterceptor implements DbmInterceptor, Ordered {
	
//	static private Logger logger = JFishLoggerFactory.getLogger(DbmThreadLocalInterceptor.class);

//	private DbmSessionFactory sessionFactory;

	public DbmThreadLocalInterceptor(DbmSessionFactory sessionFactory) {
		super();
//		this.sessionFactory = sessionFactory;
	}

	@Override
	public Object intercept(DbmInterceptorChain chain) {
		try {
			DbmThreadLocal.getOrInitContext(true);
			return chain.invoke();
		} finally {
			DbmThreadLocal.reset();
		}
	}
	

	@Override
	public int getOrder() {
		return DbmInterceptorOrder.after(DbmInterceptorOrder.DEBUG);
	}

}
