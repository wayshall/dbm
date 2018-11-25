package org.onetwo.common.dbm;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.onetwo.common.base.DbmBaseTest;
import org.onetwo.common.db.spi.BaseEntityManager;
import org.onetwo.common.dbm.model.entity.ArticleEntity;
import org.onetwo.common.dbm.model.entity.SnowflakeIdUserEntity;
import org.onetwo.common.dbm.model.entity.SnowflakeIdUserEntity.SnowflakeIdUser2Entity;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author weishao zeng
 * <br/>
 */
public class DbmSnowflakeTest extends DbmBaseTest {
	@Autowired
	BaseEntityManager entityManager;
	
	@Before
	public void before() {
		entityManager.removeAll(SnowflakeIdUserEntity.class);
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
