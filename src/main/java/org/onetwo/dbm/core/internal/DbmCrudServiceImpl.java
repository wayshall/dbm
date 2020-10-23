package org.onetwo.dbm.core.internal;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.validation.ValidationException;

import org.onetwo.common.db.BaseCrudEntityManager;
import org.onetwo.common.db.spi.BaseEntityManager;
import org.onetwo.common.exception.BusinessException;
import org.onetwo.common.spring.Springs;
import org.onetwo.common.spring.validator.ValidationBindingResult;
import org.onetwo.common.utils.Page;
import org.onetwo.dbm.core.spi.DbmEntityManager;
import org.onetwo.dbm.mapping.DbmMappedEntry;
import org.springframework.transaction.annotation.Transactional;

abstract public class DbmCrudServiceImpl<T, PK extends Serializable> extends BaseCrudEntityManager<T, PK> {

	public DbmCrudServiceImpl(Class<T> entityClass) {
		super(entityClass, null);
	}

	protected DbmCrudServiceImpl() {
		this((Class<T>)null);
	}
	
	protected DbmCrudServiceImpl(BaseEntityManager baseEntityManager) {
		super(baseEntityManager);
	}

	@Override
	@Transactional(readOnly=true)
	public T findById(PK id) {
		return super.findById(id);
	}
	
	@Override
	@Transactional(readOnly=true)
	public Number countRecord(Map<Object, Object> properties) {
		return super.countRecord(properties);
	}

	@Override
	@Transactional(readOnly=true)
	public Number countRecord(Object... params) {
		return super.countRecord(params);
	}

	@Override
	@Transactional(readOnly=true)
	public List<T> findAll() {
		return super.findAll();
	}

	@Override
	@Transactional(readOnly=true)
	public List<T> findListByProperties(Map<Object, Object> properties) {
		return super.findListByProperties(properties);
	}

	@Override
	@Transactional(readOnly=true)
	public List<T> findListByProperties(Object... properties) {
		return super.findListByProperties(properties);
	}

	@Override
	@Transactional(readOnly=true)
	public Page<T> findPage(Page<T> page, Map<Object, Object> properties) {
		return super.findPage(page, properties);
	}

	@Override
	@Transactional(readOnly=true)
	public Page<T> findPage(Page<T> page, Object... properties) {
		return super.findPage(page, properties);
	}

	@Override
	@Transactional(readOnly=true)
	public T load(PK id) {
		return super.load(id);
	}

	@Override
	@Transactional
	public T remove(T entity) {
		return super.remove(entity);
	}
	
	@Override
	@Transactional
	public void removes(Collection<T> entities) {
		super.removes(entities);
	}

	@Override
	@Transactional
	public T removeById(PK id) {
		return super.removeById(id);
	}

	@Override
	@Transactional
	public T save(T entity) {
		return super.save(entity);
	}

	/*@Override
	@Transactional
	public void persist(Object entity) {
		super.persist(entity);
	}*/

	@Override
	@Transactional(readOnly=true)
	public T findUnique(Map<Object, Object> properties) {
		return super.findUnique(properties);
	}

	@Override
	@Transactional(readOnly=true)
	public T findUnique(Object... properties) {
		return super.findUnique(properties);
	}
	
	@Transactional
	public int removeAll(){
		return getBaseEntityManager().removeAll(entityClass);
	}
	
	protected ValidationBindingResult validate(Object obj, Class<?>... groups){
		ValidationBindingResult validations = Springs.getInstance().getValidator().validate(obj);
		return validations;
	}
	
	protected void validateAndThrow(Object obj, Class<?>... groups) throws BusinessException{
		ValidationBindingResult validations = validate(obj, groups);
		if(validations.hasErrors()){
			throw new ValidationException(validations.getErrorMessagesAsString());
		}
	}
	
	protected DbmMappedEntry getMappedEntry(){
		DbmEntityManager dem = (DbmEntityManager)this.getBaseEntityManager();
		return dem.getCurrentSession().getMappedEntryManager().getEntry(entityClass);
	}
	
}
