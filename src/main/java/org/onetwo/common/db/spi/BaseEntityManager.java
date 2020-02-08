package org.onetwo.common.db.spi;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.onetwo.common.db.DbmQueryValue;
import org.onetwo.common.db.EntityManagerProvider;
import org.onetwo.common.db.ILogicDeleteEntity;
import org.onetwo.common.db.builder.QueryBuilder;
import org.onetwo.common.db.sqlext.SQLSymbolManager;
import org.onetwo.common.utils.Page;
import org.onetwo.dbm.core.spi.DbmSessionFactory;
import org.onetwo.dbm.dialet.DBDialect.LockInfo;
import org.onetwo.dbm.utils.DbmLock;

/****
 * 通用的实体查询接口
 * ByProperties后缀的方法名一般以Map为参数，其作用和没有ByProperties后缀的一样
 * @author way
 *
 */
public interface BaseEntityManager {

	public <T> T load(Class<T> entityClass, Serializable id);
	
	public <T> T findById(Class<T> entityClass, Serializable id);
	
	/***
	 * @see org.onetwo.dbm.dialet.DBDialect$LockInfo
	 * @author wayshall
	 * @param entityClass
	 * @param id
	 * @param lock
	 * @param timeoutInMillis lock forevaer if null, support: oracle, not support: mysql
	 * @return return the target or null if not found
	 */
	public <T> T lock(Class<T> entityClass, Serializable id, DbmLock lock, Integer timeoutInMillis);
	
	default <T> T lockWrite(Class<T> entityClass, Serializable id) {
		return lock(entityClass, id, DbmLock.PESSIMISTIC_WRITE, LockInfo.WAIT_FOREVER);
	}
	
	default <T> T lockRead(Class<T> entityClass, Serializable id) {
		return lock(entityClass, id, DbmLock.PESSIMISTIC_READ, LockInfo.WAIT_FOREVER);
	}

	public <T> T save(T entity);
	public <T> Collection<T> saves(Collection<T> entities);
	
	public <T> void persist(T entity);
	
	/****
	 * 根据id把实体的所有字段更新到数据库
	 * @param entity
	 */
	public void update(Object entity);
	/***
	 * 根据id把实体的非null字段更新到数据库
	 * @author wayshall
	 * @param entity
	 */
	public void dymanicUpdate(Object entity);

	/****
	 * @param entity
	 */
	public int remove(Object entity);
	
	/***
	 * 返回updateCount
	 * 注意：某些数据库或版本的jdbc driver，批量删除时，updateCount并不正确
	 * @author wayshall
	 * @param entities
	 * @return
	 */
	public <T> int removes(Collection<T> entities);
	/***
	 * 实际上是removeById的批量操作，如果removeById返回了null，则不会被放到返回的list里
	 * 可以根据实际返回的list数量和传入的集合数量是否相等来判断是否全部删除
	 * @author wayshall
	 * @param entityClass
	 * @param id
	 * @return
	 */
	public <T> Collection<T> removeByIds(Class<T> entityClass, Serializable[] id);
	/***
	 * 如果updateCount为1，则返回删除实体，否则返回null
	 * 也就是说，即使实体不存在，也不会抛错。若需要抱错，请根据返回结果是否为null判断
	 * @author wayshall
	 * @param entityClass
	 * @param id
	 * @return
	 */
	public <T> T removeById(Class<T> entityClass, Serializable id);
	/***
	 * 返回updateCount
	 * @author wayshall
	 * @param entityClass
	 * @return
	 */
	public int removeAll(Class<?> entityClass);
	
	/***
	 * 逻辑删除
	 * @author wayshall
	 * @param entity
	 */
//	public void delete(ILogicDeleteEntity entity);

	/***
	 * 逻辑删除
	 * @author wayshall
	 * @param entityClass
	 * @param id
	 * @return
	 */
//	public <T extends ILogicDeleteEntity> T deleteById(Class<T> entityClass, Serializable id);

	public <T> List<T> findAll(Class<T> entityClass);

	public Number countRecordByProperties(Class<?> entityClass, Map<Object, Object> properties);

	public Number countRecord(Class<?> entityClass, Object... params);

	/***
	 *  查找唯一记录，如果找不到返回null，如果多于一条记录，抛出异常。
	 * @param entityClass
	 * @param properties
	 * @return
	 */
	public <T> T findUniqueByProperties(Class<T> entityClass, Map<Object, Object> properties);
	public <T> T findUnique(Class<T> entityClass, Object... properties);
	
	public <T> T findOne(Class<T> entityClass, Object... properties);
	public <T> T findOneByProperties(Class<T> entityClass, Map<Object, Object> properties);


	/*****
	 * 根据属性查询列表
	 * @param entityClass
	 * @param properties
	 * @return
	 */
	public <T> List<T> findList(Class<T> entityClass, Object... properties);
	public <T> List<T> findListByProperties(Class<T> entityClass, Map<Object, Object> properties);
	
	/***
	 * @deprecated 不建议使用此方法，直接用Querys dsl api
	 * @author weishao zeng
	 * @param squery
	 * @return
	 */
	public <T> List<T> findList(QueryBuilder<T> squery);

	public <T> List<T> selectFields(Class<?> entityClass, Object[] selectFields, Object... properties);
	public <T> List<T> selectFieldsToEntity(Class<?> entityClass, Object[] selectFields, Object... properties);
	

	public <T> Page<T> findPage(final Class<T> entityClass, final Page<T> page, Object... properties);

	public <T> Page<T> findPageByProperties(final Class<T> entityClass, final Page<T> page, Map<Object, Object> properties);
	
	/***
	 * 此方法是提供给一些把QueryBuilder作为固化为实例的查询参数所用
	 * 
	 * @deprecated 不建议使用此方法，直接用Querys dsl api
	 * @author weishao zeng
	 * @param page
	 * @param query
	 * @return
	 */
	@Deprecated
	public <T> Page<T> findPage(final Page<T> page, QueryBuilder<T> query);
	
	public <T> Page<T> findPage(Page<T> page, DbmQueryValue squery);
	
	public void flush();
	
	public void clear();
	
	/***
	 * 依赖于实现
	 * @param entity
	 * @return
	 */
	public <T> T merge(T entity);
	
//	public EntityManager getEntityManager();
	
//	public DataQuery createMappingSQLQuery(String sqlString, String resultSetMapping);
	
	
	public QueryWrapper createNamedQuery(String name);
	public QueryWrapper createQuery(String sql, Map<String, Object> values);
	
	public Long getSequences(String sequenceName, boolean createIfNotExist);
	public Long getSequences(Class<?> entityClass, boolean createIfNotExist);
//	public SequenceNameManager getSequenceNameManager();
	
	public EntityManagerProvider getEntityManagerProvider();
	
	public SQLSymbolManager getSQLSymbolManager();
	
	public <T> T narrowAs(Class<T> entityManagerClass);
	
	public DbmSessionFactory getSessionFactory();
	
	<T> QueryBuilder<T> from(Class<T> entityClass);

}