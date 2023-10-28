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
import org.onetwo.common.dbm.model.hib.entity.UserEntity.UserGenders;

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
					.orQuery()
						.field("email").prelike("qq.com")
						.field("mobile").is("13666666666")
					.endSub()
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
					.andQuery()
						.field("email").prelike("qq.com")
						.orQuery()
						.field("mobile").is("13666666666")
					.endSub()
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

	@Test
	public void testOr3() {
		String userName = "test";
		QueryBuilder<UserEntity> q = Querys.from((BaseEntityManager)null, UserEntity.class)
				.where()
					.field("age").is(12)
					.field("userName").when(()->userName!=null).like(userName)
					.andQuery()
						.field("email").prelike("qq.com")
						.orQuery()
							.field("mobile").is("13666666666")
							.field("gender").is(UserGenders.MALE)
						.endSub()
					.endSub()
					.field("status").is("NORMAL")
				.end();

		ExtQueryInner extQuery = sqlSymbolManagerFactory.getJPA().createSelectQuery(UserEntity.class, "u", q.getParams());
		extQuery.build();
		
		String sql = "select u from UserEntity u where u.age = :u_age0 and u.userName like :u_userName1 and ( u.email like :u_email2 or ( u.mobile = :u_mobile3 and u.gender = :u_gender4 ) ) and u.status = :u_status5";
		String paramsting = "{u_age0=12, u_userName1=%test%, u_email2=%qq.com, u_mobile3=13666666666, u_gender4=11.0, u_status5=NORMAL}";
		System.out.println("sql: " + extQuery.getSql().trim());
		System.out.println("params value: " + extQuery.getParamsValue().getValues().toString());
		Assert.assertEquals(sql.trim(), extQuery.getSql().trim());
		Assert.assertEquals(paramsting, extQuery.getParamsValue().getValues().toString());
	}
	

	@Test
	public void testOr4() {
		String userName = "test";
		Object nullValue = null;
		QueryBuilder<UserEntity> q = Querys.from((BaseEntityManager)null, UserEntity.class)
				.where()
					.field("age")
						.isNull()
						.or()
						.lessEqual(12)
					.field("null_field")
						.is(nullValue)
					.field("userName")
						.when(()->userName!=null)
						.like(userName)
					.field("test")
						.when(()->userName==null)
						.like(userName)
					.field("email")
						.isNotNull()
						.or()
						.prelike("@test.com")
					.field("status")
						.is("NORMAL")
						.or()
						.is("DISABLED")
					.ignoreIfNull()
				.end();

		ExtQueryInner extQuery = sqlSymbolManagerFactory.getJPA().createSelectQuery(UserEntity.class, "u", q.getParams());
		extQuery.build();
		
		String sql = "select u from UserEntity u where ( u.age is null or u.age <= :u_age0 ) and u.userName like :u_userName1 and ( u.email is not null or u.email like :u_email2 ) and ( u.status = :u_status3 or u.status = :u_status4 )";
		String paramsting = "{u_age0=12, u_userName1=%test%, u_email2=%@test.com, u_status3=NORMAL, u_status4=DISABLED}";
		System.out.println("sql: " + extQuery.getSql().trim());
		System.out.println("params value: " + extQuery.getParamsValue().getValues().toString());
		Assert.assertEquals(sql.trim(), extQuery.getSql().trim());
		Assert.assertEquals(paramsting, extQuery.getParamsValue().getValues().toString());
	}
}

