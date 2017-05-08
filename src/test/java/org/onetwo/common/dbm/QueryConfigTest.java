package org.onetwo.common.dbm;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;
import org.onetwo.common.base.DbmBaseTest;
import org.onetwo.common.dbm.model.dao.UserQueryConfigDao;
import org.onetwo.common.dbm.model.entity.UserEntity;
import org.onetwo.common.utils.LangOps;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author wayshall
 * <br/>
 */
public class QueryConfigTest extends DbmBaseTest {
	public static final String USER_NAME_PREFIX = "query_config_";
	
	@Autowired
	private UserQueryConfigDao userQueryConfigDao;
	
	
	private UserEntity createUser(int index){
		UserEntity user = new UserEntity();
		user.setUserName(USER_NAME_PREFIX+index);
		user.setId(Integer.valueOf(index).longValue());
		user.setNickName("nickName"+index);
		user.setEmail("test"+index+"@test.com");
		
		return user;
	}
	@Test
	public void testSave(){
		int count = 100;
		List<UserEntity> users = LangOps.ntimesMap(count, i->{
			return createUser(i);
		});
		int res = userQueryConfigDao.batchSaveUsers(users);
		assertThat(res).isEqualTo(count);
	}

}
