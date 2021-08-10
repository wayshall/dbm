package org.onetwo.dbm.spring;

import java.util.Collection;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.onetwo.common.db.dquery.DynamicQueryObjectRegisterListener;
import org.onetwo.common.spring.Springs;
import org.onetwo.common.spring.condition.OnMissingBean;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.dbm.core.internal.DbmEntityManagerImpl;
import org.onetwo.dbm.core.internal.DbmSessionFactoryImpl;
import org.onetwo.dbm.core.spi.DbmEntityManager;
import org.onetwo.dbm.core.spi.DbmSessionFactory;
import org.onetwo.dbm.event.internal.EdgeEventBus;
import org.onetwo.dbm.exception.DbmException;
import org.onetwo.dbm.id.DbmIds;
import org.onetwo.dbm.id.SnowflakeIdGenerator;
import org.onetwo.dbm.mapping.DbmConfig;
import org.onetwo.dbm.mapping.DbmConfig.SnowflakeIdConfig;
import org.onetwo.dbm.mapping.DefaultDbmConfig;
import org.onetwo.dbm.mapping.converter.EncryptFieldValueConverter;
import org.onetwo.dbm.mapping.converter.JsonFieldValueConverter;
import org.onetwo.dbm.stat.SqlExecutedStatis;
import org.onetwo.dbm.utils.DbmUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
//@EnableConfigurationProperties({DataBaseConfig.class})
public class DbmSpringConfiguration implements ApplicationContextAware, InitializingBean, ImportAware {

	private ApplicationContext applicationContext;

	@Autowired(required=false)
	private DbmConfig dbmConfig;

//	@Autowired(required=false)
//	private Validator validator;
	
//	private String[] packagesToScan;
//	private List<String> packageNames = new ArrayList<String>();
	
	private EnableDbmAttributes enableDbmAttributes;
	
	public DbmSpringConfiguration(){
	}


	@Override
	public void setImportMetadata(AnnotationMetadata importMetadata) {
		if(importMetadata!=null){
			Map<String, Object> annotationAttributes = importMetadata.getAnnotationAttributes(EnableDbm.class.getName());
			if(annotationAttributes!=null){
				String dataSourceName = (String)annotationAttributes.get("value");
				enableDbmAttributes = new EnableDbmAttributes(dataSourceName, (String[])annotationAttributes.get("packagesToScan"));
			}
		}
	}
	
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
		Springs.initApplicationIfNotInitialized(applicationContext);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
	}
	
	/*@Bean
	public AnnotationScanBasicDynamicQueryObjectRegisterTrigger annotationScanBasicDynamicQueryObjectRegisterTrigger(){
		AnnotationScanBasicDynamicQueryObjectRegisterTrigger register = new AnnotationScanBasicDynamicQueryObjectRegisterTrigger(applicationContext);
		return register;
	}*/
	
	@Bean
	public DynamicQueryObjectRegisterListener dynamicQueryObjectRegisterListener(){
		return new DynamicQueryObjectRegisterListener();
	}
	
	/*@Bean
	public AnnotationScanBasicDynamicQueryObjectRegisterTrigger annotationScanBasicDynamicQueryObjectRegisterTrigger(){
		AnnotationScanBasicDynamicQueryObjectRegisterTrigger register = new AnnotationScanBasicDynamicQueryObjectRegisterTrigger(applicationContext);
		register.setPackageNames(getAllDbmPackageNames());
		return register;
	}*/
	
	public ApplicationContext getApplicationContex() {
		return applicationContext;
	}

	/*@Bean
	public AnnotationScanBasicDynamicQueryObjectRegister dynamicQueryObjectRegister(){
		AnnotationScanBasicDynamicQueryObjectRegister register = new AnnotationScanBasicDynamicQueryObjectRegister(this.applicationContext);
		return register;
	}*/
	public DbmConfig dbmConfig(){
//		DbmConfigFactory dbmConfigFactory = Springs.getInstance().getBean(DbmConfig.class);
		if(dbmConfig==null){
			this.dbmConfig = new DefaultDbmConfig();
		}
		this.dbmConfig.onEnableDbmAttributes(enableDbmAttributes);
		return this.dbmConfig;
	}

	@Bean
	@Autowired
	public DbmEntityManager dbmEntityManager(DbmSessionFactory sessionFactory) {
		DbmEntityManagerImpl dbmEntityManager = new DbmEntityManagerImpl(sessionFactory);

		DbmEntityManagerCreateEvent.publish(applicationContext, dbmEntityManager);
		
		return dbmEntityManager;
	}
	
	@Bean(name=DbmIds.SNOWFLAKE_BEAN_NAME)
	public SnowflakeIdGenerator dbmSnowflakeIdGenerator() {
		DbmConfig dbmConfig = dbmConfig();
		SnowflakeIdGenerator sid = null;
		SnowflakeIdConfig config = dbmConfig.getSnowflakeId();
		if (config.isAuto()) {
			sid = DbmIds.createIdGeneratorByAddress();
		} else {
			sid = DbmIds.createIdGenerator(config.getDatacenterId(), config.getMachineId());
		}
		return sid;
	}
	
	/*@Bean
	public DataQueryFilterListener dataQueryFilterListener(){
		return new DataQueryFilterListener();
	}*/

	/*@Bean
	@Autowired
	public FileNamedQueryFactory fileNamedQueryFactory(DbmEntityManager entityManager){
		JFishNamedSqlFileManager sqlFileManager = JFishNamedSqlFileManager.createNamedSqlFileManager(defaultDataBaseConfig().isWatchSqlFile());
		JFishNamedFileQueryManagerImpl fq = new JFishNamedFileQueryManagerImpl(sqlFileManager);
//		fq.initQeuryFactory(createQueryable);
		fq.setQueryProvideManager(jfishEntityManager());
		return fq;
		return entityManager.getFileNamedQueryManager();
	}*/
	
	/*@Bean
	public DbmInnerServiceRegistry dbmInnerServiceRegistry(DbmSessionFactory sessionFactory){
		DbmServiceRegistryCreateContext context = new DbmServiceRegistryCreateContext(applicationContext, sessionFactory);
		SimpleDbmInnerServiceRegistry serviceRegistry = SimpleDbmInnerServiceRegistry.obtainServiceRegistry(context);
		if(validator!=null){
			serviceRegistry.setEntityValidator(new Jsr303EntityValidator(validator));
		}
		return serviceRegistry;
	}*/
	
	private Collection<String> getAllDbmPackageNames(){
		Collection<String> packageNames = DbmUtils.getAllDbmPackageNames(applicationContext);
//		packageNames.addAll(this.packageNames);
		/*String[] modelPackages = defaultDbmConfig().getModelPackagesToScan();
		if(ArrayUtils.isNotEmpty(modelPackages)){
			packageNames.addAll(Arrays.asList(modelPackages));
		}*/
		return packageNames;
	}
	
	@Bean
	@Autowired
	public DbmSessionFactory dbmSessionFactory(ApplicationContext applicationContext, PlatformTransactionManager transactionManager, Map<String, DataSource> dataSources){
		if(LangUtils.isEmpty(dataSources)){
			throw new DbmException("dataSource not found, you must be configure a dataSource!");
		}
		DataSource dataSource = null;
		String dataSourceName = getDataSourceName();
		if(StringUtils.isBlank(dataSourceName)){
			dataSource = dataSources.size()==1?dataSources.values().iterator().next():dataSources.get(dataSourceName);
		}else{
			dataSource = dataSources.get(dataSourceName);
			if (dataSource==null) {
				throw new DbmException("dataSource not found: " + dataSourceName);
			}
		}
		DbmSessionFactoryImpl sf = new DbmSessionFactoryImpl(applicationContext, transactionManager, dataSource);
		sf.setPackagesToScan(getAllDbmPackageNames().toArray(new String[0]));
		sf.setDataBaseConfig(dbmConfig());
		
//		ConfigurableListableBeanFactory cbf = (ConfigurableListableBeanFactory)applicationContext.getAutowireCapableBeanFactory();
//		cbf.registerResolvableDependency(DbmSession.class, new DbmSessionObjectFactory(sf));
		
		return sf;
	}
	
	@Bean
	public EdgeEventBus edgeEventBus(){
		EdgeEventBus eventBus = new EdgeEventBus();
		return eventBus;
	}
	
	@Bean
	public SqlExecutedStatis sqlExecutedStatis(){
		return new SqlExecutedStatis();
	}
	
//	@Configuration
	class DbmFieldConverterConfiguration {
		@Bean
		public EncryptFieldValueConverter encryptFieldValueConverter(StandardPBEStringEncryptor encryptor) {
			EncryptFieldValueConverter converter = new EncryptFieldValueConverter();
			converter.setEncryptor(encryptor);
			return converter;
		}
		@Bean
		public StandardPBEStringEncryptor standardPBEStringEncryptor() {
			StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
			encryptor.setAlgorithm(dbmConfig().getEncrypt().getAlgorithm());
			encryptor.setPassword(dbmConfig().getEncrypt().getPassword());
			return encryptor;
		}
		@Bean
		public JsonFieldValueConverter jsonFieldValueConverter() {
			return new JsonFieldValueConverter();
		}
	}
	
	
	/*@Bean
	@Autowired
	public DbmSessionImplementor dbmDao(Map<String, DataSource> dataSources) {
		if(LangUtils.isEmpty(dataSources)){
			throw new DbmException("no dataSource found, you must be configure a dataSource!");
		}
		DataSource dataSource = null;
		String dataSourceName = getDataSourceName();
		if(StringUtils.isBlank(dataSourceName)){
			dataSource = dataSources.size()==1?dataSources.values().iterator().next():dataSources.get("dataSource");
		}else{
			dataSource = dataSources.get(dataSourceName);
		}
		DbmSessionImpl jfishDao = new DbmSessionImpl(dataSource, dbmInnerServiceRegistry(dataSource));
//		jfishDao.setNamedParameterJdbcTemplate(namedJdbcTemplate(dataSource));
		jfishDao.setDbmJdbcOperations(jdbcTemplate(dataSource));
		jfishDao.setDataBaseConfig(defaultDbmConfig());
		jfishDao.setPackagesToScan(getAllDbmPackageNames().toArray(new String[0]));
		return jfishDao;
	}*/
	
	private String getDataSourceName(){
		String ds = dbmConfig().getDataSource();
		if(StringUtils.isBlank(ds) && enableDbmAttributes!=null){
			ds = enableDbmAttributes.getDataSourceName();
		}
		return ds;
	}
	
	@Configuration
	static class JdbcTransactionManagerConfiguration {

		private final DataSource dataSource;

		JdbcTransactionManagerConfiguration(DataSource dataSource) {
			this.dataSource = dataSource;
		}

		@Bean
		@OnMissingBean(PlatformTransactionManager.class)
		public DataSourceTransactionManager transactionManager() {
			return new DataSourceTransactionManager(this.dataSource);
		}

	}
	
}
