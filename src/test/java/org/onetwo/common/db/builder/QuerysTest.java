package org.onetwo.common.db.builder;
/**
 * @author weishao zeng
 * <br/>
 */

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.onetwo.common.db.EntityManagerProvider;
import org.onetwo.common.db.filter.annotation.DataQueryFilterListener;
import org.onetwo.common.db.spi.BaseEntityManager;
import org.onetwo.common.db.sqlext.ExtQueryInner;
import org.onetwo.common.db.sqlext.JPASQLSymbolManagerImpl;
import org.onetwo.common.db.sqlext.SQLSymbolManagerFactory;
import org.onetwo.common.dbm.model.hib.entity.UserEntity;

public class QuerysTest {

	private SQLSymbolManagerFactory sqlSymbolManagerFactory;

	
	@Before
	public void setup(){
		sqlSymbolManagerFactory = SQLSymbolManagerFactory.getInstance();
		JPASQLSymbolManagerImpl jpa = new JPASQLSymbolManagerImpl();
		jpa.setListeners(Arrays.asList(new DataQueryFilterListener()));
		sqlSymbolManagerFactory.register(EntityManagerProvider.JPA, jpa);
	}
	
	/***
	 * (age = 12 and userName like %test%) or (email like %qq.com and mobile=136666666) 
	 * @author weishao zeng
	 */
	@Test
	public void testOr() {
		String userName = "test";
		QueryBuilder<UserEntity> q = Querys.from((BaseEntityManager)null, UserEntity.class)
				.where()
					.field("age").is(12)
					.field("userName").when(()->userName!=null).like(userName)
					.or()
						.field("email").prelike("qq.com")
						.field("mobile").is("13666666666")
					.end();

		ExtQueryInner extQuery = sqlSymbolManagerFactory.getJPA().createSelectQuery(UserEntity.class, "u", q.getParams());
		extQuery.build();
		
		String sql = "select u from UserEntity u where u.age = :u_age0 and u.userName like :u_userName1 or ( u.email like :u_email2 and u.mobile = :u_mobile3 )";
		String paramsting = "{u_age0=12, u_userName1=%test%, u_email2=%qq.com, u_mobile3=13666666666}";
		System.out.println("sql: " + extQuery.getSql().trim());
		System.out.println("params value: " + extQuery.getParamsValue().getValues().toString());
		Assert.assertEquals(sql.trim(), extQuery.getSql().trim());
		Assert.assertEquals(paramsting, extQuery.getParamsValue().getValues().toString());
	}
	
	/***
	 * age = 12 and userName like %test% and (email like %qq.com or mobile=136666666) 
	 * @author weishao zeng
	 */
	@Test
	public void testOr2() {
		String userName = "test";
		QueryBuilder<UserEntity> q = Querys.from((BaseEntityManager)null, UserEntity.class)
				.where()
					.field("age").is(12)
					.field("userName").when(()->userName!=null).like(userName)
					.and()
						.field("email").prelike("qq.com")
						.or()
						.field("mobile").is("13666666666")
					.end();

		ExtQueryInner extQuery = sqlSymbolManagerFactory.getJPA().createSelectQuery(UserEntity.class, "u", q.getParams());
		extQuery.build();
		
		String sql = "select u from UserEntity u where u.age = :u_age0 and u.userName like :u_userName1 "
				+ "and ( u.email like :u_email2 or ( u.mobile = :u_mobile3 ) )";
		String paramsting = "{u_age0=12, u_userName1=%test%, u_email2=%qq.com, u_mobile3=13666666666}";
		System.out.println("sql: " + extQuery.getSql().trim());
		System.out.println("params value: " + extQuery.getParamsValue().getValues().toString());
		Assert.assertEquals(sql.trim(), extQuery.getSql().trim());
		Assert.assertEquals(paramsting, extQuery.getParamsValue().getValues().toString());
	}
}

