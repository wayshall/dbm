package org.onetwo.dbm.lock;
/**
 * @author weishao zeng
 * <br/>
 */

import java.util.Date;
import java.util.Optional;
import java.util.function.Supplier;

import org.onetwo.common.db.generator.meta.TableMeta;
import org.onetwo.common.db.spi.BaseEntityManager;
import org.onetwo.dbm.core.spi.DbmSessionFactory;
import org.onetwo.dbm.exception.DbmException;
import org.onetwo.dbm.utils.DbmErrors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/***
 * 只是简单的基于数据库行锁的分布式锁，不支持重入和超时中断
 * 
 * @author way
 *
 */
public class SimpleDBLocker {
	@Autowired
	private BaseEntityManager baseEntityManager;
	private String tableName = "dbm_lock";
	
	private String lockTableCreateSql = "create table " + tableName + " (\r\n" + 
			"   id                   varchar(32) not null,\r\n" + 
			"   lock_at              datetime,\r\n" + 
			"   release_at           datetime,\r\n" + 
			"   primary key (id)\r\n" + 
			")";
	
	
	
	public SimpleDBLocker() {
	}

	@Transactional
	public void initLocker(String lockerId) {
		DbmSessionFactory sf = baseEntityManager.getSessionFactory();
		Optional<TableMeta> tableOpt = sf.getDatabaseMetaDialet().findTableMeta(tableName);
		if (!tableOpt.isPresent()) {
			sf.getSession()
				.createDbmQuery(lockTableCreateSql)
				.executeUpdate();
		}
		LockerEntity locker = baseEntityManager.findById(LockerEntity.class, lockerId);
		if (locker==null) {
			locker = new LockerEntity();
			locker.setId(lockerId);
			baseEntityManager.save(locker);
		}
	}

	/*@Transactional
	public Locker lock(String lockerId) {
		LockerEntity locker = baseEntityManager.lockWrite(LockerEntity.class, lockerId);
		if (locker==null) {
			throw new DbmException(DbmErrors.ERR_LOCK_ID_NOT_FOUND)
								.put("lockerId", lockerId);
		}
		locker.setLockAt(new Date());
		Locker lock = new Locker(locker);
		return lock;
	}*/
	
	@Transactional
	public <T> T lock(String lockerId, Supplier<T> supplier) {
		LockerEntity locker = baseEntityManager.lockWrite(LockerEntity.class, lockerId);
		if (locker==null) {
			throw new DbmException(DbmErrors.ERR_LOCK_ID_NOT_FOUND)
								.put("lockerId", lockerId);
		}
		locker.setLockAt(new Date());
		Locker lock = new Locker(locker);
		try {
			return supplier.get();
		} finally {
			lock.unlock();
		}
	}
	
	/*public <T> T lock(String lockerId, int timeoutInSeconds, Supplier<T> supplier) {
		try {
		// 事务超时时间，这个超时是非中断式触发的，这样无法实现中断式超时
			return Dbms.doInPropagation(baseEntityManager.getSessionFactory(), 
										TransactionDefinition.PROPAGATION_REQUIRES_NEW, 
										timeoutInSeconds, tx -> {
				return lock(lockerId, supplier);
			});
		} catch (TransactionTimedOutException e) {
			throw new DbmException(DbmErrors.ERR_LOCK_TIMEOUT)
							.put("lockerId", lockerId);
		}
	}*/
	
	
	public class Locker {
		private LockerEntity data;

		public Locker(LockerEntity data) {
			super();
			this.data = data;
		}
		
		public void unlock() {
			data.setReleaseAt(new Date());
			baseEntityManager.update(data);
		}
	}

}

