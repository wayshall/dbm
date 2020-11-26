package org.onetwo.common.dbm.model.service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.onetwo.common.db.spi.BaseEntityManager;
import org.onetwo.common.dbm.model.hib.entity.UserEntity;
import org.onetwo.common.dbm.model.hib.entity.UserEntity.UserGenders;
import org.onetwo.common.dbm.model.hib.entity.UserEntity.UserStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author weishao zeng
 * <br/>
 */
@Service
@Transactional
public class NoAutoIdUserService {
	@Autowired
	BaseEntityManager baseEntityManager;
	
	public int count() {
		return baseEntityManager.countRecord(UserEntity.class).intValue();
	}
	
	public UserEntity findById(Long id) {
		return baseEntityManager.findById(UserEntity.class, id);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public int deleteAll(){
		return this.baseEntityManager.removeAll(UserEntity.class);
	}

	public void insertWithStatus(int startId, int insertCount, UserStatus status){
		List<UserEntity> userlist = Stream.iterate(startId, item->item+1).limit(insertCount)
					.map(i->createUserEntity("testList", i, status))
					.collect(Collectors.toList());
		
		baseEntityManager.getSessionFactory().getSession().batchInsert(userlist);
	}
	
	public void batchInsertUser(int startId, int insertCount){
		List<UserEntity> userlist = Stream.iterate(startId, item->item+1).limit(insertCount)
					.map(i->createUserEntity("testList", i))
					.collect(Collectors.toList());
		
		baseEntityManager.getSessionFactory().getSession().batchInsert(userlist);
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public List<UserEntity> batchInsertOrUpdateUser(String prefix, int startId, int insertCount){
		List<UserEntity> userlist = Stream.iterate(startId, item->item+1).limit(insertCount)
					.map(i->createUserEntity(prefix, i))
					.collect(Collectors.toList());
		
		baseEntityManager.getSessionFactory().getSession().batchInsertOrUpdate(userlist, null);
		return userlist;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void batchInsertOrUpdateUser(List<UserEntity> userlist){
		baseEntityManager.getSessionFactory().getSession().batchInsertOrUpdate(userlist, null);
	}
	
	public void insertByStep(int startId, int insertCount){
		String userName = "unique_user_name______________________";
		Stream.iterate(startId, item->item+1).limit(insertCount).forEach(item->{
			UserEntity entity = createUserEntity("test", item);
			entity.setUserName(userName);
			baseEntityManager.save(entity);
		});
	}
	
	public UserEntity createUserEntity(String userNamePrefix,  int i){
		return createUserEntity(userNamePrefix, i, UserStatus.NORMAL);
	}
	
	public UserEntity createUserEntity(String userNamePrefix,  int i, UserStatus userStatus){
		UserEntity user = new UserEntity();
		user.setId(Long.valueOf(i));;
		user.setUserName(userNamePrefix+"-batch-"+i);
		user.setPassword("password-batch-"+i);
//		user.setCreateTime(new Date());
		user.setGender(UserGenders.MALE);
		user.setNickName("nickName-batch-"+i);
		user.setEmail("test@qq.com");
		user.setMobile("137"+i);
		user.setBirthday(new Date());
		user.setStatus(userStatus);
		return user;
	}
}

