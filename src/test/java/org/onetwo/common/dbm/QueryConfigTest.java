package org.onetwo.common.dbm;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;
import org.onetwo.common.base.DbmBaseTest;
import org.onetwo.common.db.spi.BaseEntityManager;
import org.onetwo.common.dbm.model.dao.UserQueryConfigDao;
import org.onetwo.common.dbm.model.entity.UserEntity;
import org.onetwo.common.utils.LangOps;
import org.onetwo.common.utils.Page;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author wayshall
 * <br/>
 */
public class QueryConfigTest extends DbmBaseTest {
	public static final String USER_NAME_PREFIX = "query_config_";
	
	@Autowired
	private UserQueryConfigDao userQueryConfigDao;
	@Autowired
	private BaseEntityManager baseEntityManager;
	
	
	private UserEntity createUser(int index){
		UserEntity user = new UserEntity();
		user.setUserName(USER_NAME_PREFIX+index);
		user.setId(Integer.valueOf(index).longValue());
		user.setNickName("nickName"+index);
		user.setEmail("test"+index+"@test.com");
		
		return user;
	}
	
	@Test
	public void testSaveAndFind(){
		baseEntityManager.removeAll(UserEntity.class);
		
		final int count = 100;
		List<UserEntity> users = LangOps.ntimesMap(count, i->{
			return createUser(i);
		});
		int res = userQueryConfigDao.batchSaveUsers(users);
		assertThat(res).isEqualTo(count);
		
		users = LangOps.ntimesMap(count, i->{
			UserEntity user = new UserEntity();
			user.setId(Integer.valueOf(count+i).longValue());
			user.setUserName("another query test"+i);
			return user;
		});
		res = userQueryConfigDao.batchSaveUsers(users);
		assertThat(res).isEqualTo(count);
		
		Page<UserEntity> page = userQueryConfigDao.findUserPage(Page.create(1, UserEntity.class).noLimited(), USER_NAME_PREFIX);
		assertThat(page.getSize()).isEqualTo(count);
	}

}
