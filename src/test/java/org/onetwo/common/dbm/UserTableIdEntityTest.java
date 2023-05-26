package org.onetwo.common.dbm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;
import org.onetwo.common.base.DbmBaseTest;
import org.onetwo.common.db.builder.Querys;
import org.onetwo.common.db.spi.BaseEntityManager;
import org.onetwo.common.dbm.model.entity.UserTableIdEntity;
import org.onetwo.common.utils.LangOps;
import org.springframework.test.annotation.Rollback;

/**
 * @author wayshall
 * <br/>
 */
@Rollback(false)
public class UserTableIdEntityTest extends DbmBaseTest {

	@Resource
	private BaseEntityManager entityManager;
	

	@Test
	public void testSample(){
		entityManager.removeAll(UserTableIdEntity.class);
		
		UserTableIdEntity user = new UserTableIdEntity();
		user.setUserName("dbm");
		
		//save
		Long userId = entityManager.save(user).getId();
		assertThat(userId).isNotNull();
		System.out.println("userId: " + userId);
		
		//user querys dsl api
		List<UserTableIdEntity> queryUser = Querys.from(entityManager, UserTableIdEntity.class)
											.where()
												.field("userName").is(user.getUserName())
											.end()
											.toQuery()
											.list();
		assertThat(queryUser).size().isEqualTo(1);
		assertThat(queryUser.get(0)).isEqualTo(user);
	}
	
	@Test
	public void testSaveList(){
		entityManager.removeAll(UserTableIdEntity.class);
		
		List<UserTableIdEntity> users = LangOps.generateList(1000, i->{
			UserTableIdEntity user = new UserTableIdEntity();
			user.setUserName("dbm-"+i);
			return user;
		});
		
		//save
		Collection<UserTableIdEntity> dbUsers = entityManager.saves(users);
		dbUsers.forEach(u->{
			assertThat(u.getId(), notNullValue());
			System.out.println("userId: " + u.getId());
		});
	}
}
