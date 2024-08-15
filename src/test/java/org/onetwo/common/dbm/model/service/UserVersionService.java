package org.onetwo.common.dbm.model.service;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

import org.onetwo.common.db.spi.BaseEntityManager;
import org.onetwo.common.dbm.model.entity.UserVersionEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author weishao zeng
 * <br/>
 */
//@Transactional(propagation=Propagation.REQUIRES_NEW)
@Transactional
@Service
public class UserVersionService {
	
	@Autowired
	BaseEntityManager baseEntityManager;
	
	public int removeAll() {
		return baseEntityManager.removeAll(UserVersionEntity.class);
	}
	
	public UserVersionEntity persist(UserVersionEntity user) {
		this.baseEntityManager.persist(user);
		return user;
	}
	
	public UserVersionEntity update(UserVersionEntity user) {
		this.baseEntityManager.update(user);
		return user;
	}
	
	public UserVersionEntity dymanicUpdate(UserVersionEntity user) {
		this.baseEntityManager.dymanicUpdate(user);
		return user;
	}
	
	public UserVersionEntity reload(UserVersionEntity user) {
		return this.baseEntityManager.load(UserVersionEntity.class, user.getId());
	}
	
	public UserVersionEntity updateWithCountDownLatch1(Long id, CyclicBarrier cyclicBarrier, CountDownLatch latch2)  {
		UserVersionEntity user = this.baseEntityManager.load(UserVersionEntity.class, id);
		latch2.countDown();
		try {
			cyclicBarrier.await();
		} catch (InterruptedException | BrokenBarrierException e) {
			throw new RuntimeException(e);
		}
		baseEntityManager.update(user);
		return user;
	}
	
	public UserVersionEntity update(Long id, int age) {
		UserVersionEntity user = this.baseEntityManager.load(UserVersionEntity.class, id);
		user.setAge(age);
		baseEntityManager.update(user);
		return user;
	}
	
	public UserVersionEntity save(UserVersionEntity user) {
		this.baseEntityManager.save(user);
		return user;
	}
	
	public UserVersionEntity findById(Long id) {
		return this.baseEntityManager.findById(UserVersionEntity.class, id);
	}

}

