package org.onetwo.dbm.core.internal;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.onetwo.common.date.DateUtils;
import org.onetwo.common.db.DbmQueryValue;
import org.onetwo.common.db.spi.QueryWrapper;
import org.onetwo.common.db.sql.DynamicQuery;
import org.onetwo.common.db.sql.SequenceNameManager;
import org.onetwo.common.db.sqlext.SQLSymbolManager;
import org.onetwo.common.db.sqlext.SelectExtQuery;
import org.onetwo.common.utils.Assert;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.common.utils.Page;
import org.onetwo.dbm.core.spi.DbmInnerServiceRegistry;
import org.onetwo.dbm.core.spi.DbmSession;
import org.onetwo.dbm.core.spi.DbmSessionFactory;
import org.onetwo.dbm.core.spi.DbmTransaction;
import org.onetwo.dbm.dialet.DBDialect;
import org.onetwo.dbm.event.internal.DbmSessionEventSource;
import org.onetwo.dbm.event.spi.DbmDeleteEvent;
import org.onetwo.dbm.event.spi.DbmDeleteEvent.DeleteType;
import org.onetwo.dbm.event.spi.DbmEventAction;
import org.onetwo.dbm.event.spi.DbmExtQueryEvent;
import org.onetwo.dbm.event.spi.DbmExtQueryEvent.ExtQueryType;
import org.onetwo.dbm.event.spi.DbmFindEvent;
import org.onetwo.dbm.event.spi.DbmInsertEvent;
import org.onetwo.dbm.event.spi.DbmInsertOrUpdateEvent;
import org.onetwo.dbm.event.spi.DbmLockEvent;
import org.onetwo.dbm.event.spi.DbmSessionEvent;
import org.onetwo.dbm.event.spi.DbmUpdateEvent;
import org.onetwo.dbm.exception.DbmException;
import org.onetwo.dbm.jdbc.AbstractDbmSession;
import org.onetwo.dbm.jdbc.spi.DbmJdbcOperations;
import org.onetwo.dbm.mapping.DbmConfig;
import org.onetwo.dbm.mapping.MappedEntryManager;
import org.onetwo.dbm.query.DbmQuery;
import org.onetwo.dbm.query.DbmQueryImpl;
import org.onetwo.dbm.query.DbmQueryWrapperImpl;
import org.onetwo.dbm.utils.DbmErrors;
import org.onetwo.dbm.utils.DbmLock;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.transaction.TransactionDefinition;


/****
 * 
 * @author weishao
 *
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class DbmSessionImpl extends AbstractDbmSession implements DbmSessionEventSource, DbmSession {

	final private DbmSessionFactory sessionFactory;
	private DbmTransaction transaction;
	final private long id;
	final private Date timestamp = new Date();
	private SessionTransactionType transactionType;
	private boolean closed = false;

	public DbmSessionImpl(DbmSessionFactory sessionFactory, long id, DbmTransaction transaction){
		Assert.notNull(sessionFactory);
		this.sessionFactory = sessionFactory;
		this.id = id;
		this.transaction = transaction;
		this.setDataSource(sessionFactory.getDataSource());
	}
	
	public long getId() {
		return id;
	}

	protected void errorIfClosed() {
		if (isClosed()) {
			throw new DbmException(DbmErrors.ERR_SESSION_IS_CLOSED);
		}
	}
	
	public boolean isClosed() {
		return closed;
	}

	@Override
	public void close() {
		this.transaction = null;
		this.closed = true;
	}

	public boolean useContextTransactional() {
		return transactionType == SessionTransactionType.CONTEXT_MANAGED;
	}

	public SessionTransactionType getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(SessionTransactionType mark) {
		this.transactionType = mark;
	}

	public DbmSessionFactory getSessionFactory() {
		return sessionFactory;
	}

	@Override
	public void flush() {
	}

	public DbmTransaction getTransaction() {
		this.errorIfClosed();
		return transaction;
	}

	@Override
	public DbmTransaction beginTransaction() {
		return beginTransaction(null);
	}
	
	public synchronized DbmTransaction beginTransaction(TransactionDefinition definition) {
		this.errorIfClosed();
		
		if(this.transactionType==SessionTransactionType.CONTEXT_MANAGED 
//				|| this.transactionType==SessionTransactionType.PROXY
			){
			throw new DbmException("the dbm session["+id+"] cannot start transaction manul, because it's transactional type is: " + this.transactionType);
		}
		if(transaction!=null){
			throw new DbmException("the transaction has began in this dbm session: " + id);
		}
		DbmSessionFactoryImpl sf = (DbmSessionFactoryImpl) this.sessionFactory;
		DbmTransaction transaction = sf.startNewDbmTransaction(definition);
		sf.registerSessionSynchronization(this);
		
		this.transaction = transaction;
		if(this.transactionType==null){
			this.transactionType = SessionTransactionType.MANUAL;
		}
		return transaction;
	}

	/*@Override
	protected DbmJdbcOperations createDbmJdbcOperations(DataSource dataSource) {
//		return new DbmJdbcTemplate(dataSource, getServiceRegistry().getJdbcParameterSetter());
		throw new UnsupportedOperationException();
	}*/

	/*@Override
	protected NamedJdbcTemplate createNamedJdbcTemplate(DataSource dataSource) {
		DbmNamedJdbcTemplate template = new DbmNamedJdbcTemplate(getJdbcTemplate());
		template.setJdbcParameterSetter(serviceRegistry.getJdbcParameterSetter());
		return template;
	}*/

	@Override
	protected void checkDaoConfig() {
		Assert.notNull(getDataSource());
//		Assert.notNull(dialect);
//		Assert.notNull(databaseDialetManager);
		super.checkDaoConfig();
	}

	@Override
	public DbmConfig getDataBaseConfig() {
		return this.sessionFactory.getDataBaseConfig();
	}

	public <T> int save(T entity){
		return this.insertOrUpdate(entity, true);
	}
	

	public <T> int insertOrUpdate(T entity, boolean dymanicIfUpdate){
		if(LangUtils.isNullOrEmptyObject(entity))
			throw new DbmException("entity can not be null or empty: " + entity);
		DbmInsertOrUpdateEvent event = new DbmInsertOrUpdateEvent(entity, dymanicIfUpdate, this);
//		event.setRelatedFields(relatedFields);
		this.fireEvents(event);
		return event.getUpdateCount();
	}
	
	public <T> int insert(T entity){
		return insert(entity, true);
	}
	
	protected <T> int insert(T entity, boolean fetchId){
		Assert.notNull(entity);
		DbmInsertEvent event = new DbmInsertEvent(entity, this);
		event.setFetchId(fetchId);
//		event.setRelatedFields(relatedFields);
		this.fireEvents(event);
		return event.getUpdateCount();
	}

	/*public <T> int saveRef(T entity){
		return saveRef(entity, false);
	}*/

	/*public <T> int saveRef(T entity, boolean dropAllInFirst){
		Assert.notNull(entity, "entity can not be null");
		Assert.notEmpty(relatedFields, "relatedFields can not be empty");
		JFishSaveRefEvent event = new JFishSaveRefEvent(entity, dropAllInFirst, this);
		event.setRelatedFields(relatedFields);
		this.fireEvents(event);
		return event.getUpdateCount();
	}*/

	/*public <T> int dropRef(T entity){
		Assert.notNull(entity, "entity can not be null");
		Assert.notEmpty(relatedFields, "relatedFields can not be empty");
		JFishDropRefEvent event = new JFishDropRefEvent(entity, this);
		event.setRelatedFields(relatedFields);
		this.fireEvents(event);
		return event.getUpdateCount();
	}*/

	/*public <T> int clearRef(T entity){
		Assert.notNull(entity, "entity can not be null");
//		Assert.notEmpty(relatedFields, "relatedFields can not be empty");
		JFishDropRefEvent event = new JFishDropRefEvent(entity, true, this);
//		event.setRelatedFields(relatedFields);
		this.fireEvents(event);
		return event.getUpdateCount();
	}*/
	
	@Override
	public <T> int justInsert(T entity){
		return insert(entity, false);
	}
	
	/* (non-Javadoc)
	 * @see org.onetwo.common.fish.spring.JFishDao#batchInsert(java.util.List)
	 */
	@Override
	public <T> int batchInsert(Collection<T> entities){
//		Assert.notNull(entities);
		DbmInsertEvent event = new DbmInsertEvent(entities, this);
		event.setAction(DbmEventAction.batchInsert);
//		this.fireBatchInsertEvent(event);
		this.fireEvents(event);
		return event.getUpdateCount();
	}
	
	public <T> int batchUpdate(Collection<T> entities){
		DbmUpdateEvent event = new DbmUpdateEvent(entities, this);
		event.setDynamicUpdate(false);
		event.setAction(DbmEventAction.batchUpdate);
		this.fireEvents(event);
		return event.getUpdateCount();
	}
	
	/*protected void fireEvents(JFishEventListener[] listeners, JFishEvent event){
		for(JFishEventListener listern : listeners){
			listern.doEvent(event);
		}
	}*/
	
	protected void fireEvents(DbmSessionEvent event){
		this.errorIfClosed();
		getDialect().getDbmEventListenerManager().fireEvents(event);
		/*DbmEventListener[] listeners = getDialect().getDbmEventListenerManager().getListeners(event.getAction());
		for(DbmEventListener listern : listeners){
			listern.doEvent(event);
		}*/
	}
	
	@Override
	public int update(Object entity){
		return update(entity, false);
	}
	
	@Override
	public int dymanicUpdate(Object entity){
		return update(entity, true);
	}
	
	@Override
	public int update(Object entity, boolean dymanicUpdate){
		Assert.notNull(entity);
		DbmUpdateEvent event = new DbmUpdateEvent(entity, this);
		event.setDynamicUpdate(dymanicUpdate);
//		event.setRelatedFields(relatedFields);
		this.fireEvents(event);
		return event.getUpdateCount();
	}
	
	public int delete(Object entity){
		Assert.notNull(entity);
		DbmDeleteEvent deleteEvent = new DbmDeleteEvent(entity, this);
//		deleteEvent.setRelatedFields(relatedFields);
		this.fireEvents(deleteEvent);
		return deleteEvent.getUpdateCount();
	}
	
	public int delete(Class<?> entityClass, Object id){
		Assert.notNull(id);
		DbmDeleteEvent deleteEvent = new DbmDeleteEvent(id, this);
		deleteEvent.setEntityClass(entityClass);
		deleteEvent.setDeleteType(DeleteType.BY_IDENTIFY);
		this.fireEvents(deleteEvent);
		return deleteEvent.getUpdateCount();
	}
	
	public int deleteAll(Class<?> entityClass){
		DbmDeleteEvent deleteEvent = new DbmDeleteEvent(null, this);
		deleteEvent.setEntityClass(entityClass);
		deleteEvent.setDeleteType(DeleteType.DELETE_ALL);
		this.fireEvents(deleteEvent);
		return deleteEvent.getUpdateCount();
	}
	
	@Override
	public <T> T findById(Class<T> entityClass, Serializable id){
		DbmFindEvent event = new DbmFindEvent(id, this);
		event.setEntityClass(entityClass);
		this.fireEvents(event);
		return (T)event.getResultObject();
	}
	
	@Override
	public <T> T lock(Class<T> entityClass, Serializable id, DbmLock lock, Integer timeoutInMillis) {
		DbmLockEvent event = new DbmLockEvent(id, lock, timeoutInMillis, this);
		event.setEntityClass(entityClass);
		this.fireEvents(event);
		return (T)event.getResultObject();
	}

	public <T> List<T> findAll(Class<T> entityClass){
		DbmFindEvent event = new DbmFindEvent(null, this);
		event.setFindAll(true);
		event.setEntityClass(entityClass);
		this.fireEvents(event);
		return (List<T>)event.getResultObject();
	}
	
	public <T> List<T> findByProperties(Class<T> entityClass, Map<Object, Object> properties){
		DbmExtQueryEvent event = new DbmExtQueryEvent(ExtQueryType.DEFUALT, entityClass, properties, this);
		this.fireEvents(event);
		return (List<T>)event.getResultObject();
	}
	
	public void findPageByProperties(Class<?> entityClass, Page<?> page, Map<Object, Object> properties){
		DbmExtQueryEvent event = new DbmExtQueryEvent(page, ExtQueryType.PAGINATION, entityClass, properties, this);
		this.fireEvents(event);
	}
	
	/***
	 * 查找唯一记录，如果找不到返回null，如果多于一条记录，抛出异常。
	 */
	public <T> T findUniqueByProperties(Class<T> entityClass, Map<Object, Object> properties){
		DbmExtQueryEvent event = new DbmExtQueryEvent(ExtQueryType.UNIQUE, entityClass, properties, this);
		this.fireEvents(event);
		return (T)event.getResultObject();
	}
	
	public Number countByProperties(Class<?> entityClass, Map<Object, Object> properties){
		DbmExtQueryEvent event = new DbmExtQueryEvent(ExtQueryType.COUNT, entityClass, properties, this);
		this.fireEvents(event);
		return (Number)event.getResultObject();
	}
	
	/*@Override
	public <T> List<T> queryList(Object queryableEntity){
		JFishQueryableEvent event = new JFishQueryableEvent(queryableEntity, this);
		this.fireEvents(dialect.getQueryableEventListeners(), event);
		return (List<T>)event.getResultObject();
	}*/
	
	public <T> T findUnique(String sql, Map<String, ?> params, Class<T> type){
		return findUnique(sql, params, getDefaultRowMapper(type, true));
	}
	
	/****
	 * 查找唯一结果，如果找不到则返回null，找到多个则抛异常 IncorrectResultSizeDataAccessException，详见：DataAccessUtils.requiredSingleResult
	 */
	public <T> T findUnique(String sql, Map<String, ?> params, RowMapper<T> rowMapper){
		T result = null;
		try{
			result = this.dbmJdbcOperations.queryForObject(sql, params, rowMapper);
		}catch(EmptyResultDataAccessException e){
			// 返回空集的时候，屏蔽错误，返回null
			logger.error("findUnique error: "+e.getMessage());
		}
		return result;
	}
	
	public <T> T findUnique(String sql, Object[] args, Class<T> type){
		return findUnique(sql, args, getDefaultRowMapper(type, true));
	}
	
	public <T> T findUnique(DbmQueryValue queryValue){
		/*if(queryValue.isPosition()){
			return findUnique(queryValue.getSql(), queryValue.asList().toArray(), (RowMapper<T>)getDefaultRowMapper(queryValue.getResultClass(), true));
		}else{
			return findUnique(queryValue.getSql(), queryValue.asMap(), (RowMapper<T>)getDefaultRowMapper(queryValue.getResultClass(), true));
		}*/
		return findUnique(queryValue.getSql(), queryValue.asMap(), (RowMapper<T>)getDefaultRowMapper(queryValue.getResultClass(), true));
	}
	
	public <T> T findUnique(DbmQueryValue queryValue, RowMapper<T> row){
		/*if(queryValue.isPosition()){
			return findUnique(queryValue.getSql(), queryValue.asList().toArray(), row);
		}else{
			return findUnique(queryValue.getSql(), queryValue.asMap(), row);
		}*/
		return findUnique(queryValue.getSql(), queryValue.asMap(), row);
	}
	
	public Number count(DbmQueryValue queryValue){
		Number count = null;
		SingleColumnRowMapper mapper = new SingleColumnRowMapper<Number>(Number.class);
		count = (Number)findUnique(queryValue.getCountSql(), queryValue.asMap(), mapper);
		return count;
	}
	
	public <T> T findUnique(DynamicQuery query){
		return createDbmQuery(query).getSingleResult();
	}
	
	public <T> T findUnique(String sql, Object[] args, RowMapper<T> row){
		this.errorIfClosed();
		T result = null;
		try{
			result = this.getDbmJdbcOperations().queryForObject(sql, args, row);
		}catch(EmptyResultDataAccessException e){
			logger.warn("findUnique : "+e.getMessage());
		}
		return result;
	}
	
	public <T> List<T> findList(String sql, Object[] args, RowMapper<T> rowMapper){
		List<T> result = null;
		try {
			result = this.getDbmJdbcOperations().query(sql, args, rowMapper);
		} catch (Exception e) {
			handleException("findList", sql, e);
		}
		return result;
	}
	
	protected void handleException(String tag, String msg, Exception e){
		StringBuilder newMsg = new StringBuilder();
		newMsg.append(tag).append(" error : ");
		if(e instanceof ClassCastException){
			newMsg.append("may be the query result type mapped error, check it.");
		}
		newMsg.append("[").append(msg).append("]");
		throw new DbmException(newMsg.toString(), e);
	}
	
	public <T> List<T> findList(DynamicQuery query){
		return createDbmQuery(query).getResultList();
	}
	
	public <T> List<T> findList(String sql, Object[] args, Class<T> type){
		return findList(sql, args, getDefaultRowMapper(type, false));
	}
	
	public <T> List<T> findList(String sql, Map<String, ?> params, RowMapper<T> rowMapper){
		List<T> result = this.dbmJdbcOperations.query(sql, params, rowMapper);
		return result;
	}
	
	public <T> List<T> findList(String sql, Map<String, ?> params, Class<T> type){
		return findList(sql, params, getDefaultRowMapper(type, false));
	}
	
	public <T> List<T> findList(DbmQueryValue queryValue){
		return findList(queryValue.getSql(), queryValue.asMap(), (RowMapper<T>)getDefaultRowMapper(queryValue.getResultClass(), false));
	}
	
	public <T> void findPage(Page<T> page, DbmQueryValue queryValue){
		long totalCount = count(queryValue).longValue();
		page.setTotalCount(totalCount);
		if(totalCount<1)
			return ;
//		List<T> results = findList(queryValue);a
		DbmQuery jq = createDbmQuery(queryValue.getSql(), queryValue.getResultClass());
		jq.setParameters(queryValue.asMap());
		List<T> results = jq.setFirstResult(page.getFirst()-1).setMaxResults(page.getPageSize()).getResultList();
		page.setResult(results);
	}
	
	public <T> T find(DbmQueryValue queryValue, ResultSetExtractor<T> rse){
		return this.dbmJdbcOperations.query(queryValue.getSql(), queryValue.asMap(), rse);
	}
	
	/****
	 * Extractor: RowMapperResultSetExtractor
	 */
	public <T> List<T> findList(DbmQueryValue queryValue, RowMapper<T> rowMapper){
		return this.dbmJdbcOperations.query(queryValue.getSql(), queryValue.asMap(), rowMapper);
	}
	
	public int executeUpdate(DbmQueryValue queryValue){
		int update = 0;
		update = this.dbmJdbcOperations.update(queryValue.getSql(), queryValue.asMap());
		return update;
	}
	
	public int executeUpdate(String sql, Map<String, ?> params){
		int update = this.dbmJdbcOperations.update(sql, params);
		return update;
	}
	
	public int executeUpdate(String sql, Object...args){
		int update = this.getDbmJdbcOperations().update(sql, args);
		return update;
	}
	
	public int executeUpdate(DynamicQuery query){
		int update = this.getDbmJdbcOperations().update(query.getTransitionSql(), query.getValues().toArray());
		return update;
	}
	
	public DbmQuery createDbmQuery(String sql){
		return createDbmQuery(sql, null);
	}
	
	public DbmQuery createDbmQuery(String sql, Class<?> entityClass){
		this.errorIfClosed();
		return new DbmQueryImpl(this, sql, entityClass);
	}
	
	public DbmQuery createDbmQuery(DynamicQuery query){
		query.compile();
		DbmQuery jq = createDbmQuery(query.getTransitionSql(), query.getEntityClass());
		jq.setParameters(query.getValues());
		if(query.isPage()){
			jq.setFirstResult(query.getFirstRecord());
			jq.setMaxResults(query.getMaxRecords());
		}
		return jq;
	}
	
	public SelectExtQuery createExtQuery(Class<?> entityClass, Map<Object, Object> properties){
		SelectExtQuery q = this.getSqlSymbolManager().createSelectQuery(entityClass, "ent", properties);
		return q;
	}
	
	public QueryWrapper createAsDataQuery(SelectExtQuery extQuery){
		QueryWrapper q = null;
		/*if(extQuery.isSqlQuery()){
			q = this.createAsDataQuery(extQuery.getSql(), extQuery.getEntityClass());
			q.setParameters((List)extQuery.getParamsValue().getValues());
		}else{
			q = this.createAsDataQuery(extQuery.getSql(), (Map)extQuery.getParamsValue().getValues());
		}*/
		q = this.createAsDataQuery(extQuery.getSql(), (Map)extQuery.getParamsValue().getValues());
		
		DbmQuery jq = q.getRawQuery(DbmQuery.class);
		jq.setResultClass(extQuery.getEntityClass());
		if(extQuery.needSetRange()){
			q.setLimited(extQuery.getFirstResult(), extQuery.getMaxResults());
		}
		q.setQueryConfig(extQuery.getQueryConfig());
		q.setLockInfo(extQuery.getLockInfo());
		return q;
	}
	
	public QueryWrapper createAsDataQuery(String sqlString, Class entityClass) {
		DbmQuery jq = createDbmQuery(sqlString, entityClass);
		QueryWrapper query = new DbmQueryWrapperImpl(jq);
		return query;
	}
	
	public QueryWrapper createAsDataQuery(String sql, Map<String, Object> values){
		QueryWrapper q = createAsDataQuery(sql, (Class)null);
		q.setParameters(values);
		return q;
	}

	public <T> RowMapper<T> getRowMapper(Class<T> type){
		return getDefaultRowMapper(type, false);
	}
	
	protected <T> RowMapper<T> getDefaultRowMapper(Class<T> type, boolean unique){
		return (RowMapper<T>)this.sessionFactory.getRowMapperFactory().createRowMapper(type);
	}

	protected RowMapper getColumnMapRowMapper() {
		return new ColumnMapRowMapper();
	}

	public MappedEntryManager getMappedEntryManager() {
		return this.sessionFactory.getMappedEntryManager();
	}

	public DBDialect getDialect() {
		return this.sessionFactory.getDialect();
	}


	public SQLSymbolManager getSqlSymbolManager() {
		return sessionFactory.getSqlSymbolManager();
	}


	public SequenceNameManager getSequenceNameManager() {
		return sessionFactory.getSequenceNameManager();
	}

	public DbmJdbcOperations getNamedParameterJdbcTemplate() {
		return this.dbmJdbcOperations;
	}


	public DbmInnerServiceRegistry getServiceRegistry() {
		return sessionFactory.getServiceRegistry();
	}


	@Override
	public String toString() {
		String id = DateUtils.format(DateUtils.DATEMILLS, timestamp)+"-"+getId();
		return "DbmSessionImpl [id=" + id + "]";
	}

}
