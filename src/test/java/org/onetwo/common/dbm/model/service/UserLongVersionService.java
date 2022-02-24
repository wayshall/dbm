package org.onetwo.common.dbm.model.service;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import org.onetwo.common.db.spi.BaseEntityManager;
import org.onetwo.common.dbm.model.entity.UserLongVersionEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author weishao zeng
 * <br/>
 */
//@Transactional(propagation=Propagation.REQUIRES_NEW)
@Service
@Transactional
public class UserLongVersionService {
	
	@Autowired
	BaseEntityManager baseEntityManager;
	
	public int removeAll() {
		return baseEntityManager.removeAll(UserLongVersionEntity.class);
	}
	
	public UserLongVersionEntity persist(UserLongVersionEntity user) {
		this.baseEntityManager.persist(user);
		return user;
	}
	
	public UserLongVersionEntity remove(UserLongVersionEntity user) {
		this.baseEntityManager.remove(user);
		return user;
	}
	
	
	public UserLongVersionEntity update(UserLongVersionEntity user) {
		this.baseEntityManager.update(user);
		return user;
	}
	
	public UserLongVersionEntity dymanicUpdate(UserLongVersionEntity user) {
		this.baseEntityManager.dymanicUpdate(user);
		return user;
	}
	
	public UserLongVersionEntity reload(UserLongVersionEntity user) {
		return this.baseEntityManager.load(UserLongVersionEntity.class, user.getId());
	}
	
	public UserLongVersionEntity updateWithCyclicBarrier(Long id, CyclicBarrier cyclicBarrier)  {
		UserLongVersionEntity user = this.baseEntityManager.load(UserLongVersionEntity.class, id);
		try {
			cyclicBarrier.await();
		} catch (InterruptedException | BrokenBarrierException e) {
			throw new RuntimeException(e);
		}
		baseEntityManager.update(user);
		return user;
	}
	public UserLongVersionEntity remove(Long id, CyclicBarrier cyclicBarrier) {
		// 先加载
		this.baseEntityManager.load(UserLongVersionEntity.class, id);
		try {
			cyclicBarrier.await();
		} catch (InterruptedException | BrokenBarrierException e) {
			throw new RuntimeException(e);
		}
		return this.baseEntityManager.removeById(UserLongVersionEntity.class, id);
	}
	
	public UserLongVersionEntity update(Long id, int age) {
		UserLongVersionEntity user = this.baseEntityManager.load(UserLongVersionEntity.class, id);
		user.setAge(age);
		baseEntityManager.update(user);
		return user;
	}
	
	public UserLongVersionEntity save(UserLongVersionEntity user) {
		this.baseEntityManager.save(user);
		return user;
	}
	
	public UserLongVersionEntity findById(Long id) {
		return this.baseEntityManager.findById(UserLongVersionEntity.class, id);
	}

}

