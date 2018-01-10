package org.onetwo.common.dbm;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Test;
import org.onetwo.common.base.DbmBaseTest;
import org.onetwo.common.db.spi.BaseEntityManager;
import org.onetwo.common.dbm.model.dao.UserOptionDao;
import org.onetwo.common.dbm.model.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author wayshall
 * <br/>
 */
public class UserOptionDaoTest extends DbmBaseTest {

	@Autowired
	UserOptionDao userOptionDao;
	@Autowired
	BaseEntityManager baseEntityManager;
	
	@Test
	public void testFindById(){
		Optional<UserEntity> userOpt = userOptionDao.findById(Long.MAX_VALUE);
		assertThat(userOpt).isNotNull();
		assertThat(userOpt.orElse(null)).isNull();
		
		UserEntity user = new UserEntity();
		user.setId(Long.MAX_VALUE);
		user.setUserName("testUserName");
		baseEntityManager.save(user);
		
		userOpt = userOptionDao.findById(user.getId());
		assertThat(userOpt).isNotNull();
		assertThat(userOpt.get().getId()).isEqualTo(user.getId());
		assertThat(userOpt.get().getUserName()).isEqualTo(user.getUserName());
		
	}
	
	@Test
	public void testFindByIds(){

		Optional<List<UserEntity>> usersOpt = userOptionDao.findByIds(Arrays.asList(Long.MAX_VALUE));
		assertThat(usersOpt).isNotNull();
		assertThat(usersOpt.get()).isEmpty();
		
		UserEntity user = new UserEntity();
		user.setId(Long.MAX_VALUE);
		user.setUserName("testUserName");
		baseEntityManager.save(user);
		
		UserEntity user2 = new UserEntity();
		user2.setId(Long.MAX_VALUE-1);
		user2.setUserName("testUserName2");
		baseEntityManager.save(user2);
		
		usersOpt = userOptionDao.findByIds(Arrays.asList(Long.MAX_VALUE, Long.MAX_VALUE-1));
		assertThat(usersOpt).isNotNull();
		assertThat(usersOpt.get()).size().isEqualTo(2);
		UserEntity dbuser = usersOpt.get().stream().filter(u->u.getId().equals(Long.MAX_VALUE)).collect(Collectors.toList()).get(0);
		assertThat(dbuser.getId()).isEqualTo(user.getId());
		assertThat(dbuser.getUserName()).isEqualTo(user.getUserName());
	}
}
