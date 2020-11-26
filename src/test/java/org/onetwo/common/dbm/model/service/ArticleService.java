package org.onetwo.common.dbm.model.service;

import org.onetwo.common.dbm.model.entity.ArticleEntity;
import org.onetwo.dbm.core.internal.DbmCrudServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author weishao zeng
 * <br/>
 */
@Service
@Transactional
public class ArticleService extends DbmCrudServiceImpl<ArticleEntity, Long> {

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public ArticleEntity saveForBatchUpdateForeignKey(ArticleEntity entity) {
		return super.save(entity);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public int removeAll(){
		return this.getBaseEntityManager().removeAll(ArticleEntity.class);
	}
	
}
