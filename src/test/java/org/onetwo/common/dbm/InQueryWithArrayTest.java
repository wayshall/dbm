package org.onetwo.common.dbm;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;
import org.onetwo.common.base.DbmBaseTest;
import org.onetwo.common.db.spi.BaseEntityManager;
import org.onetwo.common.dbm.model.dao.UserDao;
import org.onetwo.common.dbm.model.hib.entity.UserEntity;
import org.onetwo.common.dbm.model.hib.entity.UserEntity.UserGenders;
import org.onetwo.common.utils.JodatimeUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author weishao zeng
 * <br/>
 */
public class InQueryWithArrayTest extends DbmBaseTest {
	@Autowired
	private BaseEntityManager entityManager;
	
	@Autowired
	private UserDao userDao;
	
	@Test
	public void testFindUserWithArrayUserId() {
		entityManager.removeAll(UserEntity.class);
		Long[] userIds = new Long[]{100L, 200L, 300L, 400L};
		
		List<UserEntity> users = Stream.of(userIds).map(userId -> {
			UserEntity user = new UserEntity();
			user.setId(userId);
			user.setUserName("InQueryWithArrayTest" + userId);
			user.setBirthday(JodatimeUtils.parse("1982-05-06").toDate());
			user.setEmail("username@qq.com");
			user.setHeight(3.3f);
			user.setAge(28);
			user.setGender(UserGenders.MALE);
			return user;
		}).collect(Collectors.toList());
		
		int insertCount = userDao.batchInsertUsers(users);
		assertThat(insertCount).isEqualTo(users.size()).isEqualTo(userIds.length);
		
		List<UserEntity> dbusers = userDao.findUserWithIds(userIds);
		assertThat(dbusers.size()).isEqualTo(insertCount);
		
	}

}
