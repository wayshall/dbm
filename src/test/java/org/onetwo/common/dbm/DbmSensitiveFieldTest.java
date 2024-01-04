package org.onetwo.common.dbm;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.annotation.Resource;

import org.junit.Test;
import org.onetwo.common.base.DbmBaseTest;
import org.onetwo.common.db.spi.BaseEntityManager;
import org.onetwo.common.dbm.model.hib.entity.UserEntity;
import org.onetwo.common.dbm.model.hib.entity.UserEntity.UserGenders;
import org.onetwo.common.utils.JodatimeUtils;

//@Commit
public class DbmSensitiveFieldTest extends DbmBaseTest {

	@Resource
	private BaseEntityManager entityManager;

	

	@Test
	public void testPersist() {
		entityManager.removeAll(UserEntity.class);
		
		UserEntity user = new UserEntity();
		user.setUserName("JdbcTest");
		user.setBirthday(JodatimeUtils.parse("1982-05-06").toDate());
		user.setEmail("username@qq.com");
		user.setHeight(3.3f);
		user.setAge(28);
		user.setId(10000000000L);
		user.setGender(UserGenders.MALE);
		user.setAppCode("test1234567");
		entityManager.persist(user);
		assertThat(user.getId()).isEqualTo(10000000000L);
		
		UserEntity dbUser = entityManager.findById(UserEntity.class, user.getId());
		assertThat(dbUser.getAppCode()).isEqualTo("test1234567");
		assertThat(dbUser.getAppCodeUnsensitive()).isEqualTo("test*******");
		
	}


}
