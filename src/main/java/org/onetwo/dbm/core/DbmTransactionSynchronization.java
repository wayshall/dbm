package org.onetwo.dbm.core;

import org.onetwo.common.spring.SpringUtils;
import org.onetwo.dbm.core.internal.DbmSessionResourceHolder;
import org.onetwo.dbm.core.internal.DebugContextInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class DbmTransactionSynchronization extends TransactionSynchronizationAdapter {
	
	final private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	final private DbmSessionResourceHolder sessionHolder;
	
	public DbmTransactionSynchronization(DbmSessionResourceHolder sessionHolder){
		this.sessionHolder = sessionHolder;
	}
	
	@Override
	public int getOrder() {
		return SpringUtils.higherThan(DataSourceUtils.CONNECTION_SYNCHRONIZATION_ORDER);
	}

	/***
	 * 挂起事务
	 */
	@Override
	public void suspend() {
		TransactionSynchronizationManager.unbindResource(sessionHolder.getSessionFactory());
	}

	/***
	 * 恢复事务
	 */
	@Override
	public void resume() {
		TransactionSynchronizationManager.bindResource(sessionHolder.getSessionFactory(), sessionHolder);
	}

	@Override
	public void beforeCommit(boolean readOnly) {
		/*if(TransactionSynchronizationManager.isActualTransactionActive()){
			if(logger.isDebugEnabled()){
				logger.debug("spring transaction synchronization committing for dbmSession: {}, and dbmSession flush.", this.sessionHolder.getSession());
			}
			this.sessionHolder.getSession().flush();
		}*/
		if(logger.isDebugEnabled()){
			logger.debug("spring transaction synchronization committing for dbm session: {}, and dbm session flush.", this.sessionHolder.getSession());
		}
//		this.sessionHolder.getSession().flush();
	}

	/***
	 * 提交之后
	 */
	@Override
	public void afterCompletion(int status) {
		if(logger.isDebugEnabled()){
			logger.debug("spring transaction synchronization closing for dbm session: {}, and dbm session flush.", this.sessionHolder.getSession());
		}
		if(TransactionSynchronizationManager.getResource(this.sessionHolder.getSessionFactory())!=null) {
			TransactionSynchronizationManager.unbindResource(this.sessionHolder.getSessionFactory());
		}
		this.sessionHolder.getSession().close();
		this.sessionHolder.reset();
		DebugContextInterceptor.getDebugContext().remove();
	}
}
