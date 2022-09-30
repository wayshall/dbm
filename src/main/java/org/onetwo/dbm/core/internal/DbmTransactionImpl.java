package org.onetwo.dbm.core.internal;

import java.sql.Connection;

import org.onetwo.dbm.core.spi.DbmTransaction;
import org.onetwo.dbm.id.DbmIds;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class DbmTransactionImpl implements DbmTransaction {
	
	final private TransactionStatus status;
	final private PlatformTransactionManager transactionManager;
	final private boolean containerAutoCommit;
	private Connection connection;
	final private long id;
	
	public DbmTransactionImpl(PlatformTransactionManager transactionManager, TransactionStatus transactionStatus, boolean containerAutoCommit) {
		this.status = transactionStatus;
		this.transactionManager = transactionManager;
		this.containerAutoCommit = containerAutoCommit;
		this.id = DbmIds.getTxIdCounter().getAndIncrement();
	}
	
	public long getId() {
		return id;
	}

	public Integer getCurrentIsolationLevel(){
		return TransactionSynchronizationManager.getCurrentTransactionIsolationLevel();
	}

	@Override
	public void commit() {
		if(containerAutoCommit){
//			throw new UnsupportedOperationException("the transaction that managed by container can not be commoit manual!");
			//ignore
			return ;
		}
		this.transactionManager.commit(status);
	}

	@Override
	public void rollback() {
		if(containerAutoCommit){
//			throw new UnsupportedOperationException("the transaction that managed by container can not be rollback manual!");
			return ;
		}
		this.transactionManager.rollback(status);
	}

	public boolean isContainerAutoCommit() {
		return containerAutoCommit;
	}

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	public TransactionStatus getStatus() {
		return status;
	}

}
