package org.onetwo.dbm.core.internal;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collection;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.onetwo.dbm.annotation.DbmInterceptorFilter.InterceptorType;
import org.onetwo.dbm.annotation.DbmJdbcOperationMark;
import org.onetwo.dbm.core.internal.AbstractDbmInterceptorChain.SessionDbmInterceptorChain;
import org.onetwo.dbm.core.spi.DbmInterceptor;
import org.onetwo.dbm.core.spi.DbmInterceptorChain;
import org.onetwo.dbm.core.spi.DbmSession;
import org.onetwo.dbm.core.spi.DbmTransaction;
import org.onetwo.dbm.utils.DbmUtils;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;

@SuppressWarnings("serial")
public class DbmSessionTransactionAdvisor extends DefaultPointcutAdvisor {
	final static private AnnotationMatchingPointcut AUTO_WRAP_TRANSACTIONAL_METHOD_POINTCUT = AnnotationMatchingPointcut.forMethodAnnotation(DbmJdbcOperationMark.class);
	
	public DbmSessionTransactionAdvisor(DbmSession session, DbmInterceptorManager interceptorManager) {
		super(AUTO_WRAP_TRANSACTIONAL_METHOD_POINTCUT, new DbmSessionTransactionAdvice(session, interceptorManager));
	}	
	
	static class DbmSessionTransactionAdvice implements MethodInterceptor {
		final private DbmSession session;
		final private DbmInterceptorManager interceptorManager;
		
		public DbmSessionTransactionAdvice(DbmSession session, DbmInterceptorManager interceptorManager) {
			super();
			this.session = session;
			this.interceptorManager = interceptorManager;
		}

		@Override
		public Object invoke(MethodInvocation invocation) throws Throwable {
			Collection<DbmInterceptor> inters = interceptorManager.getInterceptors(InterceptorType.SESSION);
			DbmInterceptorChain chain = new SessionDbmInterceptorChain(session, invocation.getMethod(), invocation.getArguments(), inters);
			if(session.getTransactionType()==SessionTransactionType.PROXY){
				return invokeWithTransaction(chain);
			}else{
				return chain.invoke();
			}
		}
		
		public Object invokeWithTransaction(DbmInterceptorChain chain) throws Throwable {
			DbmTransaction transaction = session.beginTransaction();
			try {
				Object result = chain.invoke();
				transaction.commit();
				return result;
			} catch (Exception ex) {
				DbmUtils.rollbackOnException(transaction, ex);
				throw new UndeclaredThrowableException(ex, "TransactionCallback threw undeclared checked exception");
			}
		}
	}
	

}
