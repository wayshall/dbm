package org.onetwo.dbm.utils;
/**
 * @author wayshall
 * <br/>
 */
public enum DbmLock {
	/***
	 * 悲观读，共享锁
	 * 可以无锁读，共享读，不能排他读
	 */
	PESSIMISTIC_READ,
	/***
	 * 悲观写，排他锁
	 * 可以无锁读，不能有锁（排他和共享）读
	 */
	PESSIMISTIC_WRITE
}
