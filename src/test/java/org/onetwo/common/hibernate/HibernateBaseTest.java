package org.onetwo.common.hibernate;

import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.onetwo.common.dbm.model.dao.CompanyDao;
import org.onetwo.common.ds.DatasourceFactoryBean;
import org.onetwo.common.hibernate.HibernateBaseTest.HibernateTestConfig;
import org.onetwo.common.hibernate.dao.CompanyJpaRepository;
import org.onetwo.common.spring.config.JFishProfile;
import org.onetwo.common.spring.test.SpringBaseJUnitTestCase;
import org.onetwo.dbm.spring.EnableDbmRepository;
import org.onetwo.jpa.hibernate.HibernateJPAQueryProvideManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author wayshall
 * <br/>
 */
@ContextConfiguration(loader=AnnotationConfigContextLoader.class, classes=HibernateTestConfig.class)
@ActiveProfiles("dev")
public class HibernateBaseTest extends SpringBaseJUnitTestCase {
	
	@Configuration
	@JFishProfile
	@EnableTransactionManagement
	@EnableJpaRepositories(basePackageClasses=CompanyJpaRepository.class)
	@EnableDbmRepository(value="org.onetwo.common.hibernate.dao", 
						defaultQueryProviderClass=HibernateJPAQueryProvideManager.class,
						basePackageClasses=CompanyDao.class,
						autoRegister=true)
	public static class HibernateTestConfig {

		/*@Autowired
		private JFishPropertyPlaceholder configHolder;*/
		
		public HibernateTestConfig(){
		}
		
		@Bean
		public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
			LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
			em.setDataSource(dataSource);
			em.setPackagesToScan(new String[]{"org.onetwo.common.dbm.model.entity"});
			JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
			em.setJpaVendorAdapter(vendorAdapter);
			
//			Properties props = configHolder.getPropertiesWraper().getPropertiesStartWith("hibernate.", false);
			Properties props = new Properties();
			props.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL5InnoDBDialect");
			props.setProperty("hibernate.format_sql", "true");
			props.setProperty("hibernate.hbm2ddl.auto", "update");
			props.setProperty("hibernate.show_sql", "true");
			props.setProperty("hibernate.cache.region.factory_class", "org.hibernate.cache.internal.NoCachingRegionFactory");
			props.setProperty("hibernate.physical_naming_strategy", "org.onetwo.jpa.hibernate.ImprovedPhysicalNamingStrategy");
			props.setProperty("hibernate.implicit_naming_strategy", "org.hibernate.boot.model.naming.ImplicitNamingStrategyLegacyHbmImpl");
			em.setJpaProperties(props);
			return em;
		}
		
		@Bean
		public DatasourceFactoryBean datasourceFactoryBean(){
			DatasourceFactoryBean dfb = new DatasourceFactoryBean();
			dfb.setPrefix("jdbc.");
			dfb.setImplementClass(org.apache.tomcat.jdbc.pool.DataSource.class);
			return dfb;
		}
		
		@Bean
		public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
			JpaTransactionManager transactionManager = new JpaTransactionManager();
			transactionManager.setEntityManagerFactory(emf);
			transactionManager.setJpaDialect(new HibernateJpaDialect());
			return transactionManager;
		  }
		/***
		 * 异常转化
		 * 把运行时异常转化为各种DataAccessException
		 * @author wayshall
		 * @return
		 */
		@Bean
		public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
			return new PersistenceExceptionTranslationPostProcessor();
		}
		
	}

}
