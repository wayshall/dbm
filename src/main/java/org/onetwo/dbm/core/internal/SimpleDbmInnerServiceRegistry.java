package org.onetwo.dbm.core.internal;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import javax.sql.DataSource;
import javax.validation.Validator;

import org.onetwo.common.db.filequery.SqlParamterPostfixFunctions;
import org.onetwo.common.db.filter.annotation.DataQueryFilterListener;
import org.onetwo.common.db.spi.SqlParamterPostfixFunctionRegistry;
import org.onetwo.common.db.sql.SequenceNameManager;
import org.onetwo.common.db.sqlext.SQLSymbolManager;
import org.onetwo.common.spring.SpringUtils;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.dbm.core.Jsr303EntityValidator;
import org.onetwo.dbm.core.spi.DbmInnerServiceRegistry;
import org.onetwo.dbm.core.spi.DbmInterceptor;
import org.onetwo.dbm.core.spi.DbmSessionFactory;
import org.onetwo.dbm.dialet.AbstractDBDialect.DBMeta;
import org.onetwo.dbm.dialet.DBDialect;
import org.onetwo.dbm.dialet.DbmetaFetcher;
import org.onetwo.dbm.dialet.DefaultDatabaseDialetManager;
import org.onetwo.dbm.event.internal.EdgeEventBus;
import org.onetwo.dbm.exception.DbmException;
import org.onetwo.dbm.jdbc.DbmJdbcOperationsProxy;
import org.onetwo.dbm.jdbc.internal.DbmJdbcTemplate;
import org.onetwo.dbm.jdbc.internal.JdbcEventInterceptor;
import org.onetwo.dbm.jdbc.internal.SpringJdbcResultSetGetter;
import org.onetwo.dbm.jdbc.internal.SpringStatementParameterSetter;
import org.onetwo.dbm.jdbc.mapper.DbmRowMapperFactory;
import org.onetwo.dbm.jdbc.mapper.RowMapperFactory;
import org.onetwo.dbm.jdbc.spi.DbmJdbcOperations;
import org.onetwo.dbm.jdbc.spi.JdbcResultSetGetter;
import org.onetwo.dbm.jdbc.spi.JdbcStatementParameterSetter;
import org.onetwo.dbm.jpa.JPAMappedEntryBuilder;
import org.onetwo.dbm.jpa.JPASequenceNameManager;
import org.onetwo.dbm.mapping.DbmConfig;
import org.onetwo.dbm.mapping.DbmTypeMapping;
import org.onetwo.dbm.mapping.DefaultDbmConfig;
import org.onetwo.dbm.mapping.EntityValidator;
import org.onetwo.dbm.mapping.MappedEntryBuilder;
import org.onetwo.dbm.mapping.MappedEntryManager;
import org.onetwo.dbm.mapping.MultiMappedEntryListener;
import org.onetwo.dbm.mapping.MutilMappedEntryManager;
import org.onetwo.dbm.query.JFishSQLSymbolManagerImpl;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class SimpleDbmInnerServiceRegistry implements DbmInnerServiceRegistry {

	final private static LoadingCache<DbmServiceRegistryCreateContext, SimpleDbmInnerServiceRegistry> SERVICE_REGISTRY_MAPPER = CacheBuilder.newBuilder()
																						/*.weakKeys()
																						.weakValues()*/
																						.build(new CacheLoader<DbmServiceRegistryCreateContext, SimpleDbmInnerServiceRegistry>() {

																							@Override
																							public SimpleDbmInnerServiceRegistry load(DbmServiceRegistryCreateContext ctx) throws Exception {
																								return SimpleDbmInnerServiceRegistry.createServiceRegistry(ctx);
																							}
																							
																						});

	public static SimpleDbmInnerServiceRegistry obtainServiceRegistry(DbmServiceRegistryCreateContext context){
		try {
			return SERVICE_REGISTRY_MAPPER.get(context);
		} catch (ExecutionException e) {
			throw new DbmException("obtain SimpleDbmInnerServiceRegistry error: " + e.getMessage(), e);
		}
	}
	private static SimpleDbmInnerServiceRegistry createServiceRegistry(DbmServiceRegistryCreateContext context){
		SimpleDbmInnerServiceRegistry serviceRegistry = new SimpleDbmInnerServiceRegistry();
		serviceRegistry.initialize(context);
		return serviceRegistry;
	}
	
//	private final Logger logger = JFishLoggerFactory.getLogger(this.getClass());
	final private Map<String, Object> services = Maps.newConcurrentMap();

	private DBDialect dialect;
	private MappedEntryManager mappedEntryManager;
	private SQLSymbolManager sqlSymbolManager;
	private SequenceNameManager sequenceNameManager;
	private DefaultDatabaseDialetManager databaseDialetManager;
	private DbmConfig dataBaseConfig;
	private RowMapperFactory rowMapperFactory;
	private EntityValidator entityValidator;
	private JdbcStatementParameterSetter jdbcParameterSetter;
	private JdbcResultSetGetter jdbcResultSetGetter;
	private DbmTypeMapping typeMapping;
	
	

	/*@Autowired(required=false)
	private List<DbmInterceptor> interceptors;*/
	private DbmInterceptorManager interceptorManager;
	private DbmJdbcOperations dbmJdbcOperations;
	
	private SqlParamterPostfixFunctionRegistry sqlParamterPostfixFunctionRegistry;
	
	private EdgeEventBus edgeEventBus;
	
	private ApplicationContext applicationContext;
	
	private <T> T initializeComponent(T component, Class<T> componentClass, Supplier<T> initializer){
		if(component!=null){
			return component;
		}
		if(applicationContext==null){
			return initializer.get();
		}
		T componentBean = SpringUtils.getBean(applicationContext, componentClass);
		if(componentBean==null){
			if(initializer!=null){
				componentBean = initializer.get();
			}
			if(componentBean!=null){
				SpringUtils.initializeBean(applicationContext, componentBean);
			}
		}
		return componentBean;
	}
	
	@Override
	public void initialize(DbmServiceRegistryCreateContext context){
		try {
			initialize0(context);
		} catch (Exception e) {
			throw new DbmException("initialize dbm component error.", e);
		}
	}
	public void initialize0(DbmServiceRegistryCreateContext context) throws Exception{
		DataSource dataSource = context.getDataSource();
		this.applicationContext = context.getApplicationContext();
		
		dataBaseConfig = initializeComponent(dataBaseConfig, DbmConfig.class, ()->new DefaultDbmConfig());
		
		jdbcParameterSetter = initializeComponent(jdbcParameterSetter, JdbcStatementParameterSetter.class, ()->new SpringStatementParameterSetter());
		
		jdbcResultSetGetter = initializeComponent(jdbcResultSetGetter, JdbcResultSetGetter.class, ()->new SpringJdbcResultSetGetter());
		
		sqlParamterPostfixFunctionRegistry = initializeComponent(sqlParamterPostfixFunctionRegistry, SqlParamterPostfixFunctionRegistry.class, ()->new SqlParamterPostfixFunctions());
		typeMapping = initializeComponent(typeMapping, DbmTypeMapping.class, ()->new DbmTypeMapping());
		
		databaseDialetManager = initializeComponent(databaseDialetManager, DefaultDatabaseDialetManager.class, ()->{
			DefaultDatabaseDialetManager databaseDialetManager = new DefaultDatabaseDialetManager();
			return databaseDialetManager;
		});

		entityValidator = initializeComponent(entityValidator, EntityValidator.class, ()->{
			Validator validator = SpringUtils.getBean(applicationContext, Validator.class);
			if(validator!=null){
				return new Jsr303EntityValidator(validator);
			}
			return null;
		});

		if(this.dialect==null){
			DBMeta dbmeta = DbmetaFetcher.create(dataSource).getDBMeta();
//			this.dialect = JFishSpringUtils.getMatchDBDiaclet(applicationContext, dbmeta);
			this.dialect = this.databaseDialetManager.getRegistered(dbmeta.getDbName());
			if (this.dialect == null) {
				throw new IllegalArgumentException("'dialect' is required");
			}
//			LangUtils.cast(dialect, InnerDBDialet.class).setDbmeta(dbmeta);
			this.dialect.getDbmeta().setVersion(dbmeta.getVersion());
			this.dialect.initialize();
		}

		mappedEntryManager = initializeComponent(mappedEntryManager, MappedEntryManager.class, ()->{
			MutilMappedEntryManager entryManager = new MutilMappedEntryManager(this);
//			this.mappedEntryManager.initialize();
			
			List<MappedEntryBuilder> builders = LangUtils.newArrayList();
			MappedEntryBuilder builder = null;
			/*MappedEntryBuilder builder = new DbmMappedEntryBuilder(this);
			builder.initialize();
			builders.add(builder);*/
			
			builder = new JPAMappedEntryBuilder(this);
			builder.initialize();
			builders.add(builder);
//			this.mappedEntryBuilders = ImmutableList.copyOf(builders);
			entryManager.setMappedEntryBuilder(ImmutableList.copyOf(builders));
			
			MultiMappedEntryListener ml = new MultiMappedEntryListener();
			entryManager.setMappedEntryManagerListener(ml);
			return entryManager;
		});
		
		//init sql symbol
		sqlSymbolManager = initializeComponent(sqlSymbolManager, SQLSymbolManager.class, ()->{
			JFishSQLSymbolManagerImpl newSqlSymbolManager = JFishSQLSymbolManagerImpl.create(this.dialect);
//			newSqlSymbolManager.setDialect(dialect);
			newSqlSymbolManager.setMappedEntryManager(mappedEntryManager);
			newSqlSymbolManager.setListeners(Arrays.asList(new DataQueryFilterListener()));
			return newSqlSymbolManager;
		});
		
//		this.mappedEntryManager = SpringUtils.getHighestOrder(applicationContext, MappedEntryManager.class);
		rowMapperFactory = initializeComponent(rowMapperFactory, RowMapperFactory.class, ()->new DbmRowMapperFactory(mappedEntryManager, jdbcResultSetGetter));
		sequenceNameManager = initializeComponent(sequenceNameManager, SequenceNameManager.class, ()->new JPASequenceNameManager());

		
		//edgeEventBus init
		edgeEventBus = initializeComponent(this.edgeEventBus, EdgeEventBus.class, null);
		Assert.notNull(edgeEventBus, "Dbm Edge EventBus can not be null");
		
		//add
		this.interceptorManager = initializeComponent(interceptorManager, DbmInterceptorManager.class, ()->{
			List<DbmInterceptor> interceptors = Lists.newArrayList();
			interceptors.addAll(SpringUtils.getBeans(applicationContext, DbmInterceptor.class));
			if(dataBaseConfig.isEnabledDebugContext()){
				interceptors.add(new DebugContextInterceptor(context.getSessionFactory()));
			}
			interceptors.add(new SessionCacheInterceptor(context.getSessionFactory()));
			interceptors.add(new LogSqlInterceptor(dataBaseConfig, context.getSessionFactory()));
			interceptors.add(new JdbcEventInterceptor(edgeEventBus));
			/*if(this.interceptors!=null){
				interceptors.addAll(this.interceptors);
			}*/
			DbmInterceptorManager interceptorManager = new DbmInterceptorManager();
			interceptorManager.setInterceptors(interceptors);
//			interceptorManager.afterPropertiesSet();
			return interceptorManager;
		});
		
		if(this.dbmJdbcOperations==null){
			DbmJdbcTemplate jdbcTemplate = new DbmJdbcTemplate(dataSource, getJdbcParameterSetter());
//			jdbcTemplate.afterPropertiesSet();
			AspectJProxyFactory ajf = new AspectJProxyFactory(jdbcTemplate);
			ajf.setProxyTargetClass(true);
			ajf.addAspect(new DbmJdbcOperationsProxy(interceptorManager, jdbcTemplate));
			this.dbmJdbcOperations = ajf.getProxy();
		}
	}

	@Override
	public DBDialect getDialect() {
		return dialect;
	}

	@Override
	public DbmJdbcOperations getDbmJdbcOperations() {
		return dbmJdbcOperations;
	}
	public void setDbmJdbcOperations(DbmJdbcOperations dbmJdbcOperations) {
		this.dbmJdbcOperations = dbmJdbcOperations;
	}
	public void setJdbcParameterSetter(JdbcStatementParameterSetter jdbcParameterSetter) {
		this.jdbcParameterSetter = jdbcParameterSetter;
	}


	@Override
	public JdbcStatementParameterSetter getJdbcParameterSetter() {
		return jdbcParameterSetter;
	}

	@Override
	public DbmInterceptorManager getInterceptorManager() {
		return interceptorManager;
	}
	public void setInterceptorManager(DbmInterceptorManager interceptorManager) {
		this.interceptorManager = interceptorManager;
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

	@Override
	public RowMapperFactory getRowMapperFactory() {
		return rowMapperFactory;
	}

	@Override
	public <T> T getService(Class<T> clazz) {
		Assert.notNull(clazz, "class can not be null");
		return clazz.cast(getService(clazz.getName()));
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getService(String name) {
		Assert.hasText(name, "name must have text");
		return (T) services.get(name);
	}

	@Override
	public <T> DbmInnerServiceRegistry register(T service) {
		return register(service.getClass().getName(), service);
	}

	@Override
	public <T> DbmInnerServiceRegistry register(String name, T service) {
		Assert.hasText(name, "name must have text");
		Assert.notNull(service, "service can not be null");
		services.put(name, service);
		return this;
	}

	@Override
	public EntityValidator getEntityValidator() {
		return entityValidator;
	}


	public void setEntityValidator(EntityValidator entityValidator) {
		this.entityValidator = entityValidator;
	}


	@Override
	public DbmTypeMapping getTypeMapping() {
		return typeMapping;
	}


	public void setTypeMapping(DbmTypeMapping typeMapping) {
		this.typeMapping = typeMapping;
	}

	
	@Override
	public SqlParamterPostfixFunctionRegistry getSqlParamterPostfixFunctionRegistry() {
		return sqlParamterPostfixFunctionRegistry;
	}
	public void setSqlParamterPostfixFunctionRegistry(
			SqlParamterPostfixFunctionRegistry sqlParamterPostfixFunctionRegistry) {
		this.sqlParamterPostfixFunctionRegistry = sqlParamterPostfixFunctionRegistry;
	}


	public EdgeEventBus getEdgeEventBus() {
		return edgeEventBus;
	}


	public static class DbmServiceRegistryCreateContext {
		final private DbmSessionFactory sessionFactory;
		final private ApplicationContext applicationContext;
		public DbmServiceRegistryCreateContext(ApplicationContext applicationContext, DbmSessionFactory sessionFactory) {
			super();
			this.sessionFactory = sessionFactory;
			this.applicationContext = applicationContext;
		}
		public DataSource getDataSource() {
			return sessionFactory.getDataSource();
		}
		public DbmSessionFactory getSessionFactory() {
			return sessionFactory;
		}
		public ApplicationContext getApplicationContext() {
			return applicationContext;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime
					* result
					+ ((sessionFactory == null) ? 0 : sessionFactory.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DbmServiceRegistryCreateContext other = (DbmServiceRegistryCreateContext) obj;
			if (sessionFactory == null) {
				if (other.sessionFactory != null)
					return false;
			} else if (!sessionFactory.equals(other.sessionFactory))
				return false;
			return true;
		}
		
	}

}
