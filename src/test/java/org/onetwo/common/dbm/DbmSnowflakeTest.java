package org.onetwo.common.dbm;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.onetwo.common.base.DbmBaseTest;
import org.onetwo.common.concurrent.ConcurrentRunnable;
import org.onetwo.common.db.spi.BaseEntityManager;
import org.onetwo.common.dbm.model.entity.ArticleEntity;
import org.onetwo.common.dbm.model.entity.SnowflakeIdUserEntity;
import org.onetwo.common.dbm.model.entity.SnowflakeIdUserEntity.SnowflakeIdUser2Entity;
import org.onetwo.common.dbm.model.service.SnowflakeIdService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author weishao zeng
 * <br/>
 */
public class DbmSnowflakeTest extends DbmBaseTest {

	@Autowired
	private BaseEntityManager entityManager;
	
	/****
	 * 必须都要REQUIRES_NEW事务，否则死锁；
	 * 因为spring 测试的时候会自动启动一个事务，非REQUIRES_NEW的话都会使用同一个事务，如此before方法后并没有提交事务，
	 * 在执行testConcurrentGenerateIds都并发测试时，会因为等待事务都释放也造成死锁
	 */
	@Autowired
	SnowflakeIdService snowflakeIdService;
	
	@Before
	public void before() {
		snowflakeIdService.removeSnowflakeIdUsers();
		snowflakeIdService.removeArticles();
	}
	

	/***
	 * 测试并发插入snowfalekID是否有问题
	 * 
	 * @author weishao zeng
	 */
	@Test
	public void testConcurrentGenerateIds(){
		int count = 1000;
		
		ConcurrentRunnable cr = ConcurrentRunnable.create(1, () -> {
			this.snowflakeIdService.saveSnowflakeIdUsers(count);
		})
		.addRunnables(()->{
			this.snowflakeIdService.saveArticles(count);
		})
		;
		
		cr.start();
		cr.await();
	}
	
	@Test
	public void testTableIdAndSnowflakeId() {
		ArticleEntity article = new ArticleEntity();
		article.setTitle("testTableIdAndSnowflakeId");
		article.setContent("testTableIdAndSnowflakeId");
		entityManager.persist(article);
		
		assertThat(article.getId()).isNotNull();
//		assertThat(article.getTid()).isNotNull();
		assertThat(article.getDataVersion()).isEqualTo(1);
		
		entityManager.update(article);
		assertThat(article.getDataVersion()).isEqualTo(2);
	}

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
	
}
