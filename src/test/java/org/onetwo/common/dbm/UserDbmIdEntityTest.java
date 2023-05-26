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
import org.onetwo.common.dbm.model.entity.UserTableDbmIdEntity;
import org.onetwo.common.utils.LangOps;

/**
 * @author wayshall
 * <br/>
 */
public class UserDbmIdEntityTest extends DbmBaseTest {

	@Resource
	private BaseEntityManager entityManager;
	

	@Test
	public void testSaveSnowflakeUserEntity() {

		entityManager.removeAll(SnowflakeIdUserEntity.class);
		
		SnowflakeIdUserEntity user = new SnowflakeIdUserEntity();
		user.setUserName("snowflake1");
		
		entityManager.save(user);
		assertThat(user.getId()).isNotNull();
		
		SnowflakeIdUserEntity dbuser = entityManager.findById(SnowflakeIdUserEntity.class, user.getId());
		assertThat(dbuser).isNotNull();
		assertThat(dbuser.getUserName()).isEqualTo(user.getUserName());
	}
	
	@Test
	public void testSaveUserTableDbmIdEntity(){
		entityManager.removeAll(UserTableDbmIdEntity.class);
		
		UserTableDbmIdEntity user = new UserTableDbmIdEntity();
		user.setUserName("dbm");
		
		//save
		Long userId = entityManager.save(user).getId();
		assertThat(userId, notNullValue());
		
		//user querys dsl api
		UserTableDbmIdEntity queryUser = Querys.from(entityManager, UserTableDbmIdEntity.class)
											.where()
												.field("userName").is(user.getUserName())
											.end()
											.toQuery()
											.one();
		assertThat(queryUser, equalTo(user));
	}
	
	@Test
	public void testSaveList(){
		List<UserTableDbmIdEntity> users = LangOps.generateList(1000, i->{
			UserTableDbmIdEntity user = new UserTableDbmIdEntity();
			user.setUserName("dbm-"+i);
			return user;
		});
		
		//save
		Collection<UserTableDbmIdEntity> dbUsers = entityManager.saves(users);
		dbUsers.forEach(u->{
			assertThat(u.getId(), notNullValue());
		});
	}
}
