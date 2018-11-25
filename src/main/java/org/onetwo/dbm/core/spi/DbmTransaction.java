package org.onetwo.dbm.core.spi;

import java.sql.Connection;

import org.springframework.transaction.TransactionStatus;


public interface DbmTransaction {
	
	long getId();
	
	TransactionStatus getStatus();
	
	Connection getConnection();
	
	void commit();
	
	void rollback();
	

}
