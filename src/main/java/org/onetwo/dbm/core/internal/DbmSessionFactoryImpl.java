package org.onetwo.dbm.core.internal;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import javax.sql.DataSource;

import org.apache.commons.lang3.ArrayUtils;
import org.onetwo.common.db.generator.dialet.DatabaseMetaDialet;
import org.onetwo.common.db.generator.dialet.DelegateDatabaseMetaDialet;
import org.onetwo.common.db.sql.SequenceNameManager;
import org.onetwo.common.db.sqlext.SQLSymbolManager;
import org.onetwo.common.log.JFishLoggerFactory;
import org.onetwo.common.spring.Springs;
import org.onetwo.common.utils.Assert;
import org.onetwo.dbm.core.DbmTransactionSynchronization;
import org.onetwo.dbm.core.internal.SimpleDbmInnerServiceRegistry.DbmServiceRegistryCreateContext;
import org.onetwo.dbm.core.spi.DbmInnerServiceRegistry;
import org.onetwo.dbm.core.spi.DbmSession;
import org.onetwo.dbm.core.spi.DbmSessionFactory;
import org.onetwo.dbm.core.spi.DbmTransaction;
import org.onetwo.dbm.dialet.DBDialect;
import org.onetwo.dbm.dialet.DefaultDatabaseDialetManager;
import org.onetwo.dbm.exception.DbmException;
import org.onetwo.dbm.jdbc.mapper.RowMapperFactory;
import org.onetwo.dbm.mapping.DbmConfig;
import org.onetwo.dbm.mapping.MappedEntryManager;
import org.onetwo.dbm.utils.DbmTransactionSupports;
import org.onetwo.dbm.utils.DbmUtils;
import org.slf4j.Logger;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class DbmSessionFactoryImpl implements InitializingBean, DbmSessionFactory {

	final private Logger logger = JFishLoggerFactory.getLogger(this.getClass());
	
	private PlatformTransactionManager transactionManager;
	private DataSource dataSource;
	@Autowired
	private ApplicationContext applicationContext;
	

	private DBDialect dialect;
	private MappedEntryManager mappedEntryManager;

	private RowMapperFactory rowMapperFactory;
	private SQLSymbolManager sqlSymbolManager;
	private SequenceNameManager sequenceNameManager;
	
	private DefaultDatabaseDialetManager databaseDialetManager;
	protected DbmConfig dataBaseConfig;
	private DbmInnerServiceRegistry serviceRegistry;
	protected String[] packagesToScan;
	
	final private AtomicLong idGenerator = new AtomicLong(0);
	
	private DbmInterceptorManager interceptorManager;
	
	private boolean autoCreatedTransactionManager;
	
	private DatabaseMetaDialet databaseMetaDialet;
	
	public DbmSessionFactoryImpl(ApplicationContext applicationContext, PlatformTransactionManager transactionManager,
			DataSource dataSource) {
		super();
		this.transactionManager = transactionManager;
		this.dataSource = dataSource;
	}

	public DbmInterceptorManager getInterceptorManager() {
		return interceptorManager;
	}

	public void setInterceptorManager(DbmInterceptorManager interceptorManager) {
		this.interceptorManager = interceptorManager;
	}

	public boolean isAutoCreatedTransactionManager() {
		return autoCreatedTransactionManager;
	}

	@Override
	public void afterPropertiesSet() {
		Springs.initApplicationIfNotInitialized(applicationContext);
		if(transactionManager==null && applicationContext!=null){
			this.transactionManager = DbmUtils.getDataSourceTransactionManager(applicationContext, dataSource, ()->{
				if(logger.isWarnEnabled()){
					logger.warn("no transaction manager found for [], dbm auto create a new DataSourceTransactionManager and you must manager transaction manual, "
							+ "but explicit configurate spring transaction interceptor is recommended",
							dataSource);
				}
				autoCreatedTransactionManager = true;
				return new DataSourceTransactionManager(dataSource);
			});
		}
		if(serviceRegistry==null){
			DbmServiceRegistryCreateContext context = new DbmServiceRegistryCreateContext(applicationContext, this);
			this.serviceRegistry = SimpleDbmInnerServiceRegistry.obtainServiceRegistry(context);
		}
		
		this.initialize(serviceRegistry);
	}
	
	protected void initialize(DbmInnerServiceRegistry serviceRegistry) {
		/*this.serviceRegistry = new SimpleDbmInnserServiceRegistry();
		this.serviceRegistry.initialize(getDataSource(), packagesToScan);*/
//		SimpleDbmInnerServiceRegistry serviceRegistry = getServiceRegistry();
		Assert.notNull(serviceRegistry);
		
		if(dataBaseConfig==null){
			this.dataBaseConfig = serviceRegistry.getDataBaseConfig();
		}
		this.databaseDialetManager = serviceRegistry.getDatabaseDialetManager();
		this.dialect = serviceRegistry.getDialect();
		this.mappedEntryManager = serviceRegistry.getMappedEntryManager();
		this.sqlSymbolManager = serviceRegistry.getSqlSymbolManager();
		this.rowMapperFactory = serviceRegistry.getRowMapperFactory();
		this.sequenceNameManager = serviceRegistry.getSequenceNameManager();
		
		this.databaseMetaDialet = new DelegateDatabaseMetaDialet(dataSource);
		
		if(ArrayUtils.isNotEmpty(packagesToScan)){
			mappedEntryManager.scanPackages(packagesToScan);
		}

		Assert.notNull(rowMapperFactory);
		this.interceptorManager = serviceRegistry.getInterceptorManager();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> RowMapper<T> getRowMapper(Class<T> type){
		return (RowMapper<T>)this.rowMapperFactory.createRowMapper(type);
	}

	
	private DbmSession proxySession(DbmSession session){
		ProxyFactory pf = new ProxyFactory(session);
		pf.addAdvisor(new DbmSessionTransactionAdvisor(session, interceptorManager));
		return (DbmSession)pf.getProxy();
	}
	
	public Optional<DbmSession> getCurrentSession(){
		DbmSessionResourceHolder sessionHolder = (DbmSessionResourceHolder)TransactionSynchronizationManager.getResource(this);
		if(sessionHolder!=null && sessionHolder.isSynchronizedWithTransaction() 
				&& DataSourceUtils.isConnectionTransactional(sessionHolder.getConnection(), dataSource)){//return false if multip ds
			return Optional.of(sessionHolder.getSession());
		}
		return Optional.empty();
	}
	
	public DbmSession getSession(){
		DbmSession session = getRawSession();
		return proxySession(session);
	}
	
	private DbmSessionImpl createDbmSession(DbmTransaction transaction){
		DbmSessionImpl session = null;
		if(getDataBaseConfig().isEnableSessionCache()){
			session = new DbmCacheSessionImpl(this, generateSessionId(), transaction);
		}else{
			session = new DbmSessionImpl(this, generateSessionId(), transaction);
		}
		return session;
	}
	
	public DbmSession getRawSession(){
		Optional<DbmSession> sessionOpt = getCurrentSession();
		if(sessionOpt.isPresent()){
			return sessionOpt.get();
		}
		if(!TransactionSynchronizationManager.isSynchronizationActive()){
			if(this.dataBaseConfig.isAutoProxySessionTransaction()){
				TransactionSynchronizationManager.initSynchronization();
			}else{
				throw new DbmException("no transaction synchronization in current thread, you should add @Transactional to transaction method!");
			}
		}
		
		if(TransactionSynchronizationManager.isActualTransactionActive()){//transaction exists in current thread
			if(DbmTransactionSupports.currentTransactionInfo()==null){//can not get transaction info from thread
				//虽然获取不到，但实际上外部有事务管理器存在的，依然设置为CONTEXT_MANAGED
				DbmSessionImpl session = createDbmSession(null);
				session.setTransactionType(SessionTransactionType.CONTEXT_MANAGED);
				session.setDebug(getDataBaseConfig().isLogSql());
				session.setDbmJdbcOperations(getServiceRegistry().getDbmJdbcOperations());
				registerSessionSynchronization(session);
				return session;
				
			}else if(isTransactionManagerEqualsCurrentTransactionManager()){//if same transactionManager, use current thread TransactionStatus
				DbmTransaction transaction = createCurrentDbmTransaction();
				DbmSessionImpl session = createDbmSession(transaction);
				session.setTransactionType(SessionTransactionType.CONTEXT_MANAGED);
				session.setDebug(getDataBaseConfig().isLogSql());
				session.setDbmJdbcOperations(getServiceRegistry().getDbmJdbcOperations());
				registerSessionSynchronization(session);
				return session;
			}
		}
		
		//otherwise, create new session with proxy transaction that by DbmSessionTransactionAdvisor
		DbmSessionImpl session = createDbmSession(null);
		session.setTransactionType(SessionTransactionType.PROXY);
		session.setDebug(getDataBaseConfig().isLogSql());
		session.setDbmJdbcOperations(getServiceRegistry().getDbmJdbcOperations());
		return session;
	}
	
	public boolean isTransactionManagerEqualsCurrentTransactionManager(){
		PlatformTransactionManager ctm = DbmTransactionSupports.currentPlatformTransactionManager();
		return transactionManager.equals(ctm);
	}
	

	private DbmTransaction createCurrentDbmTransaction() {
		/*if(!TransactionSynchronizationManager.isActualTransactionActive()){
			throw new DbmException("no transaction active in current thread!");
		}*/

		//检查status的ds或者tm是否和sessionFactory的匹配
		DbmTransaction dt = null;
		if(isTransactionManagerEqualsCurrentTransactionManager()){
			TransactionStatus status = TransactionAspectSupport.currentTransactionStatus();
			Connection connection = DataSourceUtils.getConnection(getDataSource());
			DbmTransactionImpl transaction = new DbmTransactionImpl(transactionManager, status, true);
			transaction.setConnection(connection);
			dt = transaction;
			
		}else{
			PlatformTransactionManager ctm = DbmTransactionSupports.currentPlatformTransactionManager();
			throw new DbmException("the transactionManager["+transactionManager+"] is not equals the transactionManager["+ctm+"] in current thread ");
		}
		
		return dt;
	}
	

	DbmTransaction startNewDbmTransaction(TransactionDefinition definition) {
		if(definition==null){
			definition = new DefaultTransactionDefinition();
		}
		TransactionStatus status = transactionManager.getTransaction(definition);
		
		Connection connection = DataSourceUtils.getConnection(dataSource);
		DbmTransactionImpl transaction = new DbmTransactionImpl(transactionManager, status, false);
		transaction.setConnection(connection);
		
//		registerSessionSynchronization(session);
		
		return transaction;
	}
	
	DbmTransactionSynchronization registerSessionSynchronization(DbmSession session){
		Connection connection = DataSourceUtils.getConnection(this.dataSource);
		//sessionHolder
		DbmSessionResourceHolder sessionHolder = (DbmSessionResourceHolder)TransactionSynchronizationManager.getResource(this);
		if(sessionHolder==null) {
			sessionHolder = new DbmSessionResourceHolder(session, connection);
			TransactionSynchronizationManager.bindResource(this, sessionHolder);
		}
		//synchronization
		DbmTransactionSynchronization synchronization = new DbmTransactionSynchronization(sessionHolder);
		sessionHolder.setSynchronizedWithTransaction(true);
		TransactionSynchronizationManager.registerSynchronization(synchronization);
//		sessionHolder.requested();
		
		return synchronization;
	}
	
	
	/***
	 * 需要自己手动管理实务
	 */
	@Override
	public DbmSession openSession(){
		DbmSessionImpl session = createDbmSession(null);
		session.setDebug(getDataBaseConfig().isLogSql());
		session.setTransactionType(SessionTransactionType.MANUAL);
		session.setDbmJdbcOperations(getServiceRegistry().getDbmJdbcOperations());
		return proxySession(session);
	}
	
	protected long generateSessionId(){
		return idGenerator.getAndIncrement();
	}

	@Override
	public DbmInnerServiceRegistry getServiceRegistry() {
		return serviceRegistry;
	}

	public void setServiceRegistry(DbmInnerServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	@Override
	public RowMapperFactory getRowMapperFactory() {
		return rowMapperFactory;
	}

	@Override
	public PlatformTransactionManager getTransactionManager() {
		return transactionManager;
	}

	@Override
	public DataSource getDataSource() {
		return dataSource;
	}

	@Override
	public DBDialect getDialect() {
		return dialect;
	}

	public void setDataBaseConfig(DbmConfig dataBaseConfig) {
		this.dataBaseConfig = dataBaseConfig;
	}

	@Override
	public MappedEntryManager getMappedEntryManager() {
		return mappedEntryManager;
	}

	@Override
	public SQLSymbolManager getSqlSymbolManager() {
		return sqlSymbolManager;
	}

	@Override
	public SequenceNameManager getSequenceNameManager() {
		return sequenceNameManager;
	}

	@Override
	public DefaultDatabaseDialetManager getDatabaseDialetManager() {
		return databaseDialetManager;
	}

	@Override
	public DbmConfig getDataBaseConfig() {
		return dataBaseConfig;
	}

	public void setPackagesToScan(String[] packagesToScan) {
		this.packagesToScan = packagesToScan;
	}

	public DatabaseMetaDialet getDatabaseMetaDialet() {
		return databaseMetaDialet;
	}

	@Override
	public String toString() {
		return "DbmSessionFactoryImpl [transactionManager="
				+ transactionManager + ", dataSource=" + dataSource
				+ ", dialect=" + dialect + ", packagesToScan="
				+ Arrays.toString(packagesToScan)
				+ ", autoCreatedTransactionManager="
				+ autoCreatedTransactionManager + "]";
	}

}
