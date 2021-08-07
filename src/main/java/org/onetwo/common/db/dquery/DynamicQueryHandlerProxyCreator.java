package org.onetwo.common.db.dquery;

import java.lang.reflect.Method;
import java.util.Optional;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.onetwo.common.db.dquery.annotation.DbmRepository;
import org.onetwo.common.db.filequery.DbmNamedSqlFileManager;
import org.onetwo.common.db.spi.NamedQueryFile;
import org.onetwo.common.db.spi.NamedSqlFileManager;
import org.onetwo.common.db.spi.QueryProvideManager;
import org.onetwo.common.log.JFishLoggerFactory;
import org.onetwo.common.propconf.ResourceAdapter;
import org.onetwo.common.spring.SpringUtils;
import org.onetwo.dbm.core.spi.DbmEntityManager;
import org.onetwo.dbm.exception.DbmException;
import org.onetwo.dbm.exception.FileNamedQueryException;
import org.onetwo.dbm.utils.Dbms;
import org.slf4j.Logger;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;

import com.google.common.cache.LoadingCache;

public class DynamicQueryHandlerProxyCreator implements InitializingBean, ApplicationContextAware, FactoryBean<Object>, BeanNameAware {
	
	public static final String ATTR_SQL_FILE = "sqlFile";

	protected final Logger logger = JFishLoggerFactory.getLogger(this.getClass());
	
	private LoadingCache<Method, DynamicMethod> methodCache;
	private ApplicationContext applicationContext;
	protected Class<?> interfaceClass;
	private Object targetObject;
	private ResourceAdapter<?> sqlFile;

	private String beanName;
	
	private Class<?> defaultQueryProvideManagerClass = DbmEntityManager.class;
	
	public DynamicQueryHandlerProxyCreator(Class<?> interfaceClass, LoadingCache<Method, DynamicMethod> methodCache) {
		super();
		this.interfaceClass = interfaceClass;
		this.methodCache = methodCache;
	}
	
	private Optional<DbmRepositoryAttrs> findDbmRepositoryAttrs(){
		DbmRepository dbmRepository = this.interfaceClass.getAnnotation(DbmRepository.class);
		if(dbmRepository==null){
			/*QueryRepository queryRepository = this.interfaceClass.getAnnotation(QueryRepository.class);
			if(queryRepository==null){
				return Optional.empty();
			}
			DbmRepositoryAttrs attrs = new DbmRepositoryAttrs(queryRepository.provideManager(), queryRepository.dataSource());
			return Optional.of(attrs);*/
			return Optional.empty();
		}
		DbmRepositoryAttrs attrs = new DbmRepositoryAttrs(dbmRepository.queryProviderName(), dbmRepository.queryProviderClass(), dbmRepository.dataSource());
		return Optional.of(attrs);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(defaultQueryProvideManagerClass, "defaultQueryProvideManagerClass can not be null");
		
		QueryProvideManager queryProvideManager = findQueryProvideManager();
		
		NamedSqlFileManager namedSqlFileManager = (DbmNamedSqlFileManager)queryProvideManager.getFileNamedQueryManager().getNamedSqlFileManager();
		Assert.notNull(namedSqlFileManager, "namedSqlFileManager can not be null");
		
		DbmSqlFileResource<?> sqlFile = getSqlFile(queryProvideManager.getDataSource());
		Assert.notNull(sqlFile, "sqlFile can not be null");

		logger.info("initialize dynamic query proxy[{}] for : {}", beanName, sqlFile);
		NamedQueryFile queryFile = namedSqlFileManager.buildSqlFile(sqlFile);
//		interfaceClass = ReflectUtils.loadClass(info.getNamespace());
		if(!interfaceClass.getName().equals(queryFile.getNamespace())){
			throw new FileNamedQueryException("namespace error:  interface->" + interfaceClass+", namespace->"+queryFile.getNamespace());
		}
//		targetObject = new JDKProxyDynamicQueryHandler(queryProvideManager, methodCache, interfaceClass).getQueryObject();
		targetObject = new SpringProxyDynamicQueryHandler(queryProvideManager, methodCache, interfaceClass).getQueryObject();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected DbmSqlFileResource<?> getSqlFile(DataSource dataSource){
		return new DbmSqlFileResource(sqlFile, interfaceClass);
	}
	
	protected QueryProvideManager findQueryProvideManagerByClass(Class<?> clazz){
		Object bean = SpringUtils.getBean(applicationContext, clazz);
		if(!QueryProvideManager.class.isInstance(bean)){
			throw new DbmException("the query provide manager is not a QueryProvideManager : " + clazz);
		}
		return (QueryProvideManager) bean;
	}
	
	private QueryProvideManager findQueryProvideManager(){
		QueryProvideManager queryProvideManager;
		Optional<DbmRepositoryAttrs> dbmRepositoryAttrs = findDbmRepositoryAttrs();
		if(!dbmRepositoryAttrs.isPresent()){
			queryProvideManager = findQueryProvideManagerByClass(defaultQueryProvideManagerClass);
		}else{
			DbmRepositoryAttrs attrs = dbmRepositoryAttrs.get();
			if(StringUtils.isNotBlank(attrs.provideManager())){
				queryProvideManager = SpringUtils.getBean(applicationContext, attrs.provideManager());
			}else if(attrs.hasProvideManagerClass()){
				queryProvideManager = findQueryProvideManagerByClass(attrs.getProvideManagerClass());
			}else if(StringUtils.isNotBlank(attrs.dataSource())){
				DataSource dataSource = SpringUtils.getBean(applicationContext, attrs.dataSource());
				if(dataSource==null){
					throw new DbmException("DataSource not found: " + attrs.dataSource());
				}
				queryProvideManager = (QueryProvideManager)Dbms.obtainBaseEntityManager(dataSource);
			}else{
				queryProvideManager = findQueryProvideManagerByClass(defaultQueryProvideManagerClass);
			}
		}
		if(queryProvideManager==null){
			throw new FileNamedQueryException("no QueryProvideManager found!");
		}
		return queryProvideManager;
	}

	@Override
	public Object getObject() throws Exception {
		return targetObject;
	}

	@Override
	public Class<?> getObjectType() {
		return interfaceClass;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public void setSqlFile(ResourceAdapter<?> sqlFile) {
		this.sqlFile = sqlFile;
	}

	@Override
	public void setBeanName(String name) {
		this.beanName = name;
	}

	/*public void setQueryProvideManager(QueryProvideManager queryProvideManager) {
		this.queryProvideManager = queryProvideManager;
	}*/
	
	final public void setDefaultQueryProvideManagerClass(Class<? extends QueryProvideManager> defaultQueryProvideManagerClass) {
		this.defaultQueryProvideManagerClass = defaultQueryProvideManagerClass;
	}

	static class DbmRepositoryAttrs {
		final private String provideManager;
		final private Class<?> provideManagerClass;
		final private String dataSource;
		
		public DbmRepositoryAttrs(String provideManager, Class<?> provideManagerClass, String dataSource) {
			super();
			this.provideManager = provideManager;
			this.provideManagerClass = provideManagerClass;
			this.dataSource = dataSource;
		}
		
		public DbmRepositoryAttrs(String provideManager, String dataSource) {
			this(provideManager, null, dataSource);
		}
		public String provideManager() {
			return provideManager;
		}
		public String dataSource() {
			return dataSource;
		}
		
		public boolean hasProvideManagerClass(){
			return provideManagerClass!=null && provideManagerClass!=QueryProvideManager.class;
		}

		@SuppressWarnings("unchecked")
		public <T> Class<T> getProvideManagerClass() {
			return (Class<T>)provideManagerClass;
		}
	}
	
}
