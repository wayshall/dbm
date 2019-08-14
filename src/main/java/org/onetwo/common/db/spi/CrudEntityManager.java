package org.onetwo.common.db.spi;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.onetwo.common.db.builder.QueryBuilder;
import org.onetwo.common.utils.Page;

public interface CrudEntityManager<T, PK extends Serializable> {
	
	int batchInsert(Collection<T> entities);
 
	T load(PK id);
	
	T findById(PK id);
	
	Optional<T> findOptionalById(PK id);

	/*****
	 * insert or update
	 * @param entity
	 * @return
	 */
	T save(T entity);

	void update(T entity);
	void dymanicUpdate(T entity);
	void persist(T entity);
	
//	T createNew(T entity);

//	T updateAttributes(T entity);

	T remove(T entity);
	
	void removes(Collection<T> entities);

	T removeById(PK id);
	Collection<T> removeByIds(PK[] id);

	int removeAll();

	List<T> findAll();

	Number countRecord(Map<Object, Object> properties);

	Number countRecord(Object... params);

	List<T> findListByProperties(Object... properties);

	List<T> findListByProperties(Map<Object, Object> properties);
	
	List<T> findListByProperties(QueryBuilder<T> squery);
	
	List<T> findListByExample(Object example);

	Page<T> findPage(final Page<T> page, Object... properties);

	Page<T> findPage(final Page<T> page, Map<Object, Object> properties);
	
	Page<T> findPageByExample(final Page<T> page, Object example);
	
	void findPage(final Page<T> page, QueryBuilder<T> query);

	T findUnique(Object... properties);
	
	T findUnique(Map<Object, Object> properties);
	
	T findOne(Object... properties);
	
	BaseEntityManager getBaseEntityManager();

}
