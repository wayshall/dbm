package org.onetwo.common.base;

import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.junit.Test;
import org.onetwo.common.dbm.PackageInfo;
import org.onetwo.common.ds.DatasourceFactoryBean;
import org.onetwo.common.spring.config.JFishProfile;
import org.onetwo.common.spring.config.JFishPropertyPlaceholder;
import org.onetwo.common.spring.test.SpringBaseJUnitTestCase;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author wayshall
 * <br/>
 */
@ActiveProfiles("dev")
public class HibernateBaseTest extends SpringBaseJUnitTestCase {
	
	@Test
	public void testEmpty(){
	}
	
	
	@Configuration
	@JFishProfile
	@EnableTransactionManagement
	@EnableJpaRepositories(basePackageClasses=PackageInfo.class)
	public static class HibernateTestConfig {

		@Autowired
		private JFishPropertyPlaceholder configHolder;
		
		@Bean
		public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
			LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
			em.setDataSource(dataSource);
			em.setPackagesToScan(new String[]{"org.onetwo.common.dbm.model.entity"});
			JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
			em.setJpaVendorAdapter(vendorAdapter);
			
			Properties props = configHolder.getPropertiesWraper().getPropertiesStartWith("hibernate.", false);
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
