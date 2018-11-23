package org.onetwo.common.dbm;

import static org.assertj.core.api.Assertions.assertThat;
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
import org.onetwo.common.dbm.model.entity.SnowflakeIdUserEntity;
import org.onetwo.common.dbm.model.entity.SnowflakeIdUserEntity.SnowflakeIdUser2Entity;
import org.onetwo.common.dbm.model.entity.UserTableIdEntity;
import org.onetwo.common.utils.LangOps;

/**
 * @author wayshall
 * <br/>
 */
public class UserTableIdEntityTest extends DbmBaseTest {

	@Resource
	private BaseEntityManager entityManager;
	
	@Test
	public void testSaveSnowflakeUser() {
		SnowflakeIdUserEntity user = new SnowflakeIdUserEntity();
		user.setUserName("snowflake");
		entityManager.save(user);
		assertThat(user.getId()).isNotNull();
		
		SnowflakeIdUserEntity dbuser = entityManager.load(SnowflakeIdUserEntity.class, user.getId());
		assertThat(dbuser.getUserName()).isEqualTo(user.getUserName());
		

		SnowflakeIdUser2Entity user2 = new SnowflakeIdUser2Entity();
		user2.setUserName("snowflake2");
		entityManager.save(user2);
		assertThat(user2.getId()).isNotNull();
		
		SnowflakeIdUser2Entity dbuser2 = entityManager.load(SnowflakeIdUser2Entity.class, user2.getId());
		assertThat(dbuser2.getUserName()).isEqualTo(user2.getUserName());
	}
	

	@Test
	public void testSample(){
		UserTableIdEntity user = new UserTableIdEntity();
		user.setUserName("dbm");
		
		//save
		Long userId = entityManager.save(user).getId();
		assertThat(userId, notNullValue());
		
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
		});
	}
}
