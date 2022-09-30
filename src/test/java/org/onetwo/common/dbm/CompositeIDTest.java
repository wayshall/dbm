package org.onetwo.common.dbm;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;
import org.onetwo.common.base.DbmBaseTest;
import org.onetwo.common.db.spi.BaseEntityManager;
import org.onetwo.common.dbm.model.entity.ColumnArticleEntity;
import org.onetwo.common.dbm.model.entity.ColumnArticleEntity.ColumnArticleId;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;

/**
 * @author weishao zeng
 * <br/>
 */
public class CompositeIDTest extends DbmBaseTest {
	
	@Autowired
	BaseEntityManager baseEntityManager;
	
	@Test
	public void testSimpleCrud() {
		List<ColumnArticleEntity> colArticles = Lists.newArrayList();
		
		ColumnArticleEntity toLocalColArt = null;
		
		ColumnArticleEntity colArticle = new ColumnArticleEntity();
		colArticle.setArticleId(100L);
		colArticle.setColumnId(2L);
		colArticle.setHeadline(true);
		colArticles.add(colArticle);
		toLocalColArt = colArticle;
		
		colArticle = new ColumnArticleEntity();
		colArticle.setArticleId(200L);
		colArticle.setColumnId(2L);
		colArticle.setHeadline(true);
		colArticles.add(colArticle);
		
		colArticle = new ColumnArticleEntity();
		colArticle.setArticleId(300L);
		colArticle.setColumnId(2L);
		colArticle.setHeadline(false);
		colArticles.add(colArticle);
		
		baseEntityManager.saves(colArticles);
		
		int count = baseEntityManager.countRecord(ColumnArticleEntity.class).intValue();
		assertThat(count).isEqualTo(colArticles.size());
		
		
		
		ColumnArticleId cid = new ColumnArticleId(toLocalColArt.getColumnId(), toLocalColArt.getArticleId());
		ColumnArticleEntity dbColArticle = baseEntityManager.load(ColumnArticleEntity.class, cid);
		assertThat(dbColArticle).isNotNull();
		assertThat(dbColArticle.getColumnId()).isEqualTo(toLocalColArt.getColumnId());
		assertThat(dbColArticle.getArticleId()).isEqualTo(toLocalColArt.getArticleId());
		
		dbColArticle.setHeadline(false);
		baseEntityManager.update(dbColArticle);
		dbColArticle = baseEntityManager.load(ColumnArticleEntity.class, cid);
		assertThat(dbColArticle.isHeadline()).isFalse();
		

		dbColArticle.setHeadline(true);
		baseEntityManager.save(dbColArticle);
		dbColArticle = baseEntityManager.findUnique(ColumnArticleEntity.class, "columnId", dbColArticle.getColumnId(), "articleId", dbColArticle.getArticleId());
		assertThat(dbColArticle.isHeadline()).isTrue();
		
		ColumnArticleEntity deleteEntity = baseEntityManager.removeById(ColumnArticleEntity.class, toLocalColArt.getId());
		assertThat(deleteEntity).isNotNull();
		assertThat(deleteEntity.getColumnId()).isEqualTo(toLocalColArt.getColumnId());
		assertThat(deleteEntity.getArticleId()).isEqualTo(toLocalColArt.getArticleId());
		

		int deleteCount = baseEntityManager.remove(colArticles.get(colArticles.size()-1));
		assertThat(deleteCount).isEqualTo(1);
		
		count = baseEntityManager.countRecord(ColumnArticleEntity.class).intValue();
		assertThat(count).isEqualTo(colArticles.size()-2);
	}

}
