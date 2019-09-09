package org.onetwo.common.dbm;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;

import org.junit.Test;
import org.onetwo.common.base.DbmBaseTest;
import org.onetwo.common.db.spi.BaseEntityManager;
import org.onetwo.common.dbm.model.entity.UserAutoidEntity;
import org.onetwo.common.dbm.model.entity.UserAutoidEntity.UserStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

/**
 * @author wayshall
 * <br/>
 */
@Rollback(false)
public class EncryptFieldTest extends DbmBaseTest {
	
	@Autowired
	private BaseEntityManager baseEntityManager;


	private UserAutoidEntity createUserAutoidEntity(int i){
		String userNamePrefix = "EncryptFieldTest";;
		UserAutoidEntity user = new UserAutoidEntity();
		user.setUserName(userNamePrefix+"-insert-"+i);
		user.setPassword("password-insert-"+i);
		user.setGender(i%2);
		user.setNickName("nickName-insert-"+i);
		user.setEmail("test@qq.com");
		user.setMobile("137"+i);
		user.setBirthday(new Date());
		user.setStatus(UserStatus.NORMAL);
		return user;
	}
	
	
	@Test
	public void testSave(){
		baseEntityManager.removeAll(UserAutoidEntity.class);
		
		String password = "encryped-passowrd";
		UserAutoidEntity user = createUserAutoidEntity(1);
		user.setPassword(password);
		baseEntityManager.save(user);
		
		UserAutoidEntity dbuser = baseEntityManager.load(UserAutoidEntity.class, user.getId());
		assertThat(dbuser.getPassword()).isEqualTo(password);
	}
}
