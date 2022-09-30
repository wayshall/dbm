package org.onetwo.common.dbm.model.service;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.onetwo.common.db.spi.BaseEntityManager;
import org.onetwo.common.dbm.model.dao.UserAutoidDao;
import org.onetwo.common.dbm.model.dao.UserAutoidDao2;
import org.onetwo.common.dbm.model.entity.UserAutoidEntity;
import org.onetwo.common.dbm.model.entity.UserAutoidEntity.UserStatus;
import org.onetwo.common.utils.CUtils;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.common.utils.Page;
import org.onetwo.dbm.core.spi.DbmSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserAutoidServiceImpl implements UserAutoidService {
	
	/*@Resource
	private UserAutoidDao userAutoidDao;*/

	@Resource
	private DataSource dataSource;
	@Resource
	private DbmSessionFactory sessionFactory;
	
	@Resource
	private UserAutoidDao2 userAutoidDao2;
	
	@Resource
	private UserAutoidDao userAutoidDao;
	@Autowired
	private BaseEntityManager baseEntityManager;

	public int removeByUserName(String userName){
		return this.userAutoidDao.removeByUserName(userName);
	}
	
	public void removeAll() {
		this.baseEntityManager.removeAll(UserAutoidEntity.class);
	}
	
	/* (non-Javadoc)
	 * @see org.onetwo.test.jorm.model.service.UserAutoidService#deleteAll()
	 */
	@Override
	public int deleteAll(){
		return this.sessionFactory.getSession().deleteAll(UserAutoidEntity.class);
	}
	
	/* (non-Javadoc)
	 * @see org.onetwo.test.jorm.model.service.UserAutoidService#saveList(java.lang.String, java.util.Date, int)
	 */
	@Override
	public int saveList(String userNamePrefix, Date birthday, int count){ 
		List<UserAutoidEntity> list = LangUtils.newArrayList();
		for (int i = 0; i < count; i++) {
			UserAutoidEntity user = new UserAutoidEntity();
			user.setUserName(userNamePrefix+"-batch-"+i);
			user.setPassword("password-batch-"+i);
//			user.setCreateTime(new Date());
			user.setGender(i%2);
			user.setNickName("nickName-batch-"+i);
			user.setEmail("test@qq.com");
			user.setMobile("137"+i);
			user.setStatus(UserStatus.NORMAL);
			user.setBirthday(birthday);
			
			list.add(user);
		}
		int insertCount = sessionFactory.getSession().save(list);
		return insertCount;
	}
	
	/* (non-Javadoc)
	 * @see org.onetwo.test.jorm.model.service.UserAutoidService#findUserAutoIdEntity(java.lang.String, java.util.Date)
	 */
	@Override
	public List<UserAutoidEntity> findUserAutoIdEntity(String userName, Date birthday){
		return sessionFactory.getSession().findByProperties(UserAutoidEntity.class, CUtils.asMap(
																"userName:like", userName,
																"birthday", birthday
																));
	}
	
	/* (non-Javadoc)
	 * @see org.onetwo.test.jorm.model.service.UserAutoidService#update(java.util.List)
	 */
	@Override
	public int update(List<UserAutoidEntity> users){
		return sessionFactory.getSession().update(users);
	}
	
	/* (non-Javadoc)
	 * @see org.onetwo.test.jorm.model.service.UserAutoidService#delete(java.util.List)
	 */
	@Override
	public int delete(List<UserAutoidEntity> users){
		return sessionFactory.getSession().delete(users);
	}
	
	public int daoBatchInsert(String userNamePrefix, UserStatus status, Date birthday, int count){ 
		List<UserAutoidEntity> users = LangUtils.newArrayList();
		for (int i = 0; i < count; i++) {
			UserAutoidEntity user = new UserAutoidEntity();
			user.setUserName(userNamePrefix+"-batch-"+i);
			user.setPassword("password-batch-"+i);
//			user.setCreateTime(new Date());
			user.setGender(i%2);
			user.setNickName("nickName-batch-"+i);
			user.setEmail("test@qq.com");
			user.setMobile("137"+i);
			user.setStatus(status);
			user.setBirthday(birthday);
			
			users.add(user);
		}
		return this.userAutoidDao.batchInsert(users);
	}
	
	public int daoBatchInsert2(String userNamePrefix, int count){ 
		List<UserAutoidEntity> users = LangUtils.newArrayList();
		for (int i = 0; i < count; i++) {
			UserAutoidEntity user = new UserAutoidEntity();
			user.setUserName(userNamePrefix+"-batch-"+i);
			user.setPassword("password-batch-"+i);
//			user.setCreateTime(new Date());
			user.setGender(i%2);
			user.setNickName("nickName-batch-"+i);
			user.setEmail("test@qq.com");
			user.setMobile("137"+i);
			
			users.add(user);
		}
		this.userAutoidDao.batchInsert2(users, new Date());
		return count;
	}
	
	public void findUserPage(Page<UserAutoidEntity> page, String userName){
		this.userAutoidDao2.findUserPage(page, userName);
	}

	public List<UserAutoidEntity> findUserList(String status){
		return this.userAutoidDao2.findUserList(status);
	}
	
	public List<UserAutoidEntity> findAllByUserNamePrefix(String userNamePrefix) {
		return baseEntityManager.from(UserAutoidEntity.class)
							.where()
								.field("userName").postlike(userNamePrefix)
							.toQuery()
							.list();
	}
	
	public int batchUpdate(Collection<UserAutoidEntity> users ) {
		return baseEntityManager.getSessionFactory().getSession().batchUpdate(users);
	}
	
}
