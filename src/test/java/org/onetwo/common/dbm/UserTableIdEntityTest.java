package org.onetwo.common.dbm;

import static org.hamcrest.CoreMatchers.equalTo;
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
		UserTableIdEntity user = new UserTableIdEntity();
		user.setUserName("dbm");
		
		//save
		Long userId = entityManager.save(user).getId();
		assertThat(userId, notNullValue());
		System.out.println("userId: " + userId);
		
		//user querys dsl api
		UserTableIdEntity queryUser = Querys.from(entityManager, UserTableIdEntity.class)
											.where()
												.field("userName").is(user.getUserName())
											.end()
											.toQuery()
											.one();
		assertThat(queryUser, equalTo(user));
	}
	
	@Test
	public void testSaveList(){
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
