package org.onetwo.common.dbm;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import javax.annotation.Resource;

import org.junit.Test;
import org.onetwo.common.base.DbmBaseTest;
import org.onetwo.common.db.BaseEntityManager;
import org.onetwo.common.db.builder.Querys;
import org.onetwo.common.dbm.model.entity.UserTableIdEntity;

/**
 * @author wayshall
 * <br/>
 */
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
		
		//user querys dsl api
		UserTableIdEntity queryUser = Querys.from(entityManager, UserTableIdEntity.class)
											.where()
												.field("userName").is(user.getUserName())
											.end()
											.toQuery()
											.one();
		assertThat(queryUser, is(user));
		
	}
}
