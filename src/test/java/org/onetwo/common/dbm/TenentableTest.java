package org.onetwo.common.dbm;

import static org.assertj.core.api.Assertions.assertThat;

import javax.annotation.Resource;

import org.junit.Test;
import org.onetwo.common.base.DbmBaseTest;
import org.onetwo.common.db.spi.BaseEntityManager;
import org.onetwo.common.db.sqlext.ExtQuery.K;
import org.onetwo.common.dbm.model.entity.ArticleEntity;
import org.onetwo.common.dbm.model.entity.TenentBaseEntity;

/**
 * @author wayshall
 * <br/>
 */
public class TenentableTest extends DbmBaseTest {
	@Resource
	private BaseEntityManager entityManager;
	
	@Test
	public void testInsert(){
		ArticleEntity article = new ArticleEntity();
		article.setTitle("测试");
		article.setContent("测试内容");
		article = entityManager.save(article);

		assertThat(article.getTenentId()).isEqualTo(TenentBaseEntity.FIXED_TENENT_ID);
		assertThat(article.getClientId()).isEqualTo(TenentBaseEntity.FIXED_CLIENT_ID);
		
		article = entityManager.findOne(ArticleEntity.class, "id", article.getId(), K.DEBUG, true);
		assertThat(article.getTenentId()).isEqualTo(TenentBaseEntity.FIXED_TENENT_ID);
		assertThat(article.getClientId()).isEqualTo(TenentBaseEntity.FIXED_CLIENT_ID);
	}

}
