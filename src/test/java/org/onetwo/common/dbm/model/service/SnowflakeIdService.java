package org.onetwo.common.dbm.model.service;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import org.onetwo.common.db.spi.BaseEntityManager;
import org.onetwo.common.dbm.model.entity.ArticleEntity;
import org.onetwo.common.dbm.model.entity.SnowflakeIdUserEntity;
import org.onetwo.common.utils.LangUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author weishao zeng
 * <br/>
 */
@Transactional(propagation=Propagation.REQUIRES_NEW)
@Service
public class SnowflakeIdService {
	
	@Autowired
	private BaseEntityManager baseEntityManager;
	

	private Set<Long> idSet = new ConcurrentSkipListSet<>();
	
	public void removeSnowflakeIdUsers() {
		baseEntityManager.removeAll(SnowflakeIdUserEntity.class);
	}

	public void removeArticles() {
		baseEntityManager.removeAll(ArticleEntity.class);
	}
	
	public void saveSnowflakeIdUsers(int count) {
		for (int i = 0; i < count; i++) {
			SnowflakeIdUserEntity user = new SnowflakeIdUserEntity();
			user.setUserName(LangUtils.randomUUID());
			baseEntityManager.persist(user);
			
			long id = user.getId();
			if(idSet.contains(id)){
				throw new RuntimeException("same id:"+id);
			}
			idSet.add(id);
		}
	}
	
	public void saveArticles(int count) {
		for (int i = 0; i < count; i++) {
			ArticleEntity article = new ArticleEntity();
			article.setTitle("testTableIdAndSnowflakeId");
			article.setContent("testTableIdAndSnowflakeId");
			baseEntityManager.persist(article);
			
			long id = article.getId();
			if(idSet.contains(id)){
				throw new RuntimeException("same id:"+id);
			}
			idSet.add(id);
		}
	}

}
