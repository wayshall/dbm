package org.onetwo.common.dbm;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;
import org.onetwo.common.base.DbmBaseTest;
import org.onetwo.common.dbm.model.dao.UserDao;
import org.onetwo.common.dbm.model.entity.UserTableIdEntity;
import org.onetwo.common.utils.LangOps;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author wayshall
 * <br/>
 */
public class CustomDaoTest extends DbmBaseTest {
	
	@Autowired
	private UserDao userDao;
	
	@Test
	public void test(){
		int total = 100;
		List<UserTableIdEntity> users = LangOps.ntimesMap(total, i->{
			UserTableIdEntity user = new UserTableIdEntity();
			user.setUserName(i+"_test+");
			return user;
		});
		int res = this.userDao.batchInsert(users);
		assertThat(res).isEqualTo(total);
	}

}
