package org.onetwo.common.hibernate;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;
import org.onetwo.common.dbm.model.hib.entity.UserEntity;
import org.onetwo.common.hibernate.dao.UserQueryHibernateDao;
import org.onetwo.common.utils.LangOps;
import org.onetwo.common.utils.Page;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author wayshall
 * <br/>
 */
public class UserQueryHibernateDaoTest extends HibernateBaseTest {
	public static final String USER_NAME_PREFIX = "UserQueryHibernateDao_";
	
	@Autowired
	private UserQueryHibernateDao userQueryHibernateDao;

	private UserEntity createUser(int index){
		UserEntity user = new UserEntity();
		user.setUserName(USER_NAME_PREFIX+index);
		user.setId(Integer.valueOf(index).longValue());
		user.setNickName("nickName"+index);
		user.setEmail("test"+index+"@test.com");
		
		return user;
	}
	
	@Test
	public void test(){
		final int count = 100;
		List<UserEntity> users = LangOps.ntimesMap(count, i->{
			return createUser(i);
		});
		int res = userQueryHibernateDao.batchSaveUsers(users);
		assertThat(res).isEqualTo(count);
		

		users = LangOps.ntimesMap(count, i->{
			UserEntity user = new UserEntity();
			user.setId(Integer.valueOf(count+i).longValue());
			user.setUserName("another query test"+i);
			return user;
		});
		res = userQueryHibernateDao.batchSaveUsers(users);
		assertThat(res).isEqualTo(count);
		
		Page<UserEntity> page = userQueryHibernateDao.findUserPage(Page.create(1, UserEntity.class).noLimited(), USER_NAME_PREFIX);
		assertThat(page.getSize()).isEqualTo(count);
	}

}
