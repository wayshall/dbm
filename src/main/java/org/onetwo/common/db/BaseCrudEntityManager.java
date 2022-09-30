package org.onetwo.common.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.onetwo.common.db.builder.QueryBuilder;
import org.onetwo.common.db.builder.Querys;
import org.onetwo.common.db.builder.WhereCauseBuilder;
import org.onetwo.common.db.spi.BaseEntityManager;
import org.onetwo.common.db.spi.CrudEntityManager;
import org.onetwo.common.log.JFishLoggerFactory;
import org.onetwo.common.reflect.ReflectUtils;
import org.onetwo.common.spring.Springs;
import org.onetwo.common.utils.Page;
import org.onetwo.dbm.core.spi.DbmEntityManager;
import org.slf4j.Logger;
import org.springframework.transaction.annotation.Transactional;

@SuppressWarnings({"unchecked"})
public class BaseCrudEntityManager<T, PK extends Serializable> implements CrudEntityManager<T, PK> {

 
	protected final Logger logger = JFishLoggerFactory.getLogger(this.getClass());
	protected Class<T> entityClass;
	protected volatile BaseEntityManager baseEntityManager;

	
	public BaseCrudEntityManager(Class<T> entityClass, BaseEntityManager baseEntityManager){
		if(entityClass==null){
			this.entityClass = (Class<T>)ReflectUtils.getSuperClassGenricType(this.getClass(), BaseCrudEntityManager.class);
		}else{
			this.entityClass = entityClass;
		}
		this.baseEntityManager = baseEntityManager;
	}
	
	public BaseCrudEntityManager(BaseEntityManager baseEntityManager){
		this((Class<T>)null, baseEntityManager);
	}

	public BaseEntityManager getBaseEntityManager() {
		BaseEntityManager bem = this.baseEntityManager;
		if(bem==null){
			bem = Springs.getInstance().getBean(BaseEntityManager.class);
			Objects.requireNonNull(bem, "BaseEntityManager not found");
			if(this.baseEntityManager==null){
				this.baseEntityManager = bem;
			}
		}
		return bem;
	}

	@Transactional
	@Override
	public int batchInsert(Collection<T> entities) {
		return getBaseEntityManager().getSessionFactory().getSession().batchInsert(entities);
	}

	@Transactional(readOnly=true)
	@Override
	public T findById(PK id) {
		return (T)getBaseEntityManager().findById(entityClass, id);
	}

	@Transactional(readOnly=true)
	@Override
	public Optional<T> findOptionalById(PK id) {
		return Optional.ofNullable(getBaseEntityManager().findById(entityClass, id));
	}


	@Transactional(readOnly=true)
	@Override
	public Number countRecord(Map<Object, Object> properties) {
		return getBaseEntityManager().countRecordByProperties(entityClass, properties);
	}

	@Transactional(readOnly=true)
	@Override
	public Number countRecord(Object... params) {
		return getBaseEntityManager().countRecord(entityClass, params);
	}

	@Transactional(readOnly=true)
	@Override
	public List<T> findAll() {
		return getBaseEntityManager().findAll(entityClass);
	}

	@Transactional(readOnly=true)
	@Override
	public List<T> findListByProperties(Map<Object, Object> properties) {
		return getBaseEntityManager().findListByProperties(entityClass, properties);
	}

	@Transactional(readOnly=true)
	@Override
	public List<T> findListByProperties(Object... properties) {
		return getBaseEntityManager().findList(entityClass, properties);
	}

	@Transactional(readOnly=true)
	@Override
	public Page<T> findPage(Page<T> page, Map<Object, Object> properties) {
		getBaseEntityManager().findPageByProperties(entityClass, page, properties);
		return page;
	}

	@Transactional(readOnly=true)
	@Override
	public Page<T> findPage(Page<T> page, Object... properties) {
		getBaseEntityManager().findPage(entityClass, page, properties);
		return page;
	}
//	
//	public void delete(ILogicDeleteEntity entity){
//		entity.deleted();
//		this.save(entity);
//	}
//
//	public <T extends ILogicDeleteEntity> T deleteById(Class<T> entityClass, Serializable id){
//		Object entity = this.findById(entityClass, id);
//		if(entity==null)
//			return null;
//		if(!ILogicDeleteEntity.class.isAssignableFrom(entity.getClass())){
//			throw new ServiceException("实体不支持逻辑删除，请实现相关接口！");
//		}
//		T logicDeleteEntity = (T) entity;
//		logicDeleteEntity.deleted();
//		this.save(logicDeleteEntity);
//		return logicDeleteEntity;
//	}
	@Transactional(readOnly=true)
	@Override
	public T load(PK id) {
		return (T)getBaseEntityManager().load(entityClass, id);
	}

	/****
	 * 删除数据
	 * 与BaseEntityManager不同，执行此方法时，若实体实现了逻辑删除接口ILogicDeleteEntity，则只是更新状态
	 */
	@Transactional
	@Override
	public T remove(T entity) {
//		if (entity instanceof ILogicDeleteEntity) {
//			((ILogicDeleteEntity)entity).deleted();
//			getBaseEntityManager().update(entity);
//		} else {
//			getBaseEntityManager().remove(entity);
//		}
		getBaseEntityManager().remove(entity);
		return entity;
	}

	/***
	 * 删除数据
	 * 与BaseEntityManager不同，执行此方法时，若实体实现了逻辑删除接口ILogicDeleteEntity，则只是更新状态
	 */
	@Transactional
	@Override
	public void removes(Collection<T> entities) {
		entities.forEach(entity -> remove(entity));
	}

	/***
	 * 删除数据
	 * 与BaseEntityManager不同，执行此方法时，若实体实现了逻辑删除接口ILogicDeleteEntity，则只是更新状态
	 */
	@Transactional
	@Override
	public T removeById(PK id) {
		T entity = load(id);
		return remove(entity);
	}
	
	@Transactional
	@Override
	public Collection<T> removeByIds(PK[] ids){
		List<T> list = new ArrayList<>(ids.length);
		Stream.of(ids).forEach(id->list.add(removeById(id)));
		return list;
	}


	@Transactional
	public T save(T entity) {
		return getBaseEntityManager().save(entity);
	}

	@Transactional
	@Override
	public void update(T entity) {
		getBaseEntityManager().update(entity);
	}

	@Transactional
	@Override
	public void dymanicUpdate(T entity) {
		getBaseEntityManager().dymanicUpdate(entity);
	}

	@Transactional
	@Override
	public void persist(T entity) {
		getBaseEntityManager().persist(entity);
	}

	@Transactional(readOnly=true)
	@Override
	public T findUnique(Map<Object, Object> properties) {
		return (T)getBaseEntityManager().findUniqueByProperties(entityClass, properties);
	}

	@Transactional(readOnly=true)
	@Override
	public T findUnique(Object... properties) {
		return (T)getBaseEntityManager().findUnique(entityClass, properties);
	}

	@Transactional(readOnly=true)
	@Override
	public T findOne(Object... properties) {
		return (T)getBaseEntityManager().findOne(entityClass, properties);
	}

	/***
	 * 
	 * @deprecated 不建议使用此方法，直接用Querys dsl api
	 */
	@Deprecated
	@Transactional(readOnly=true)
	@Override
	public List<T> findListByProperties(QueryBuilder<T> query) {
		return getBaseEntityManager().findList(query);
	}

	/***
	 * 
	 * @deprecated 不建议使用此方法，直接用Querys dsl api
	 */
	@Deprecated
	@Transactional(readOnly=true)
	@Override
	public void findPage(final Page<T> page, QueryBuilder<T> query) {
		getBaseEntityManager().findPage(page, query);
	}

	@Transactional
	@Override
	public int removeAll() {
		return getBaseEntityManager().removeAll(entityClass);
	}

	@Transactional(readOnly=true)
	@Override
	public List<T> findListByExample(Object example) {
		return Querys.from(getBaseEntityManager(), entityClass)
				.where()
				.addFields(example)
				.ignoreIfNull()
				.end()
				.toQuery()
				.list();
	}

	@Transactional(readOnly=true)
	@Override
	public Page<T> findPageByExample(Page<T> page, Object example) {
		return Querys.from(getBaseEntityManager(), entityClass)
						.where()
						.addFields(example)
						.ignoreIfNull()
						.end()
						.toQuery()
						.page(page);
	}

	protected WhereCauseBuilder<T> where(){
		DbmEntityManager dem = (DbmEntityManager)this.getBaseEntityManager();
		return dem.from(entityClass).where();
	}
}
