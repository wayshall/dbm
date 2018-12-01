package org.onetwo.common.dbm;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.onetwo.common.base.DbmBaseTest;
import org.onetwo.common.db.spi.BaseEntityManager;
import org.onetwo.common.dbm.model.entity.ColumnArticleEntity;
import org.onetwo.common.dbm.model.entity.ColumnArticleEntity.ColumnArticleId;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author weishao zeng
 * <br/>
 */
public class CompositeIDTest extends DbmBaseTest {
	
	@Autowired
	BaseEntityManager baseEntityManager;
	
	@Test
	public void testSave() {
		ColumnArticleEntity colArticle = new ColumnArticleEntity();
		colArticle.setArticleId(1L);
		colArticle.setColumnId(2L);
		colArticle.setHeadline(true);
		
		baseEntityManager.save(colArticle);
		
		ColumnArticleEntity dbColArticle = baseEntityManager.load(ColumnArticleEntity.class, new ColumnArticleId(colArticle.getColumnId(), colArticle.getArticleId()));
		assertThat(dbColArticle).isNotNull();
		assertThat(dbColArticle.getColumnId()).isEqualTo(colArticle.getColumnId());
		assertThat(dbColArticle.getArticleId()).isEqualTo(colArticle.getArticleId());
	}

}
