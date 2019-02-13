package org.springframework.data.jdbc.dbm;

import javax.sql.DataSource;

import org.junit.Test;
import org.onetwo.common.ds.DatasourceFactoryBean;
import org.onetwo.common.spring.config.JFishProfile;
import org.onetwo.common.spring.test.SpringBaseJUnitTestCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jdbc.dbm.SpringDataJdbcBaseTest.SpringDataJdbcTestConfig;
import org.springframework.data.jdbc.dbm.repository.TestUserRepository;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.data.jdbc.repository.config.JdbcConfiguration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * @author wayshall
 * <br/>
 */
@ContextConfiguration(loader=AnnotationConfigContextLoader.class, classes=SpringDataJdbcTestConfig.class)
@ActiveProfiles("dev")
public class SpringDataJdbcBaseTest extends SpringBaseJUnitTestCase {
	
	@Test
	public void testStartup() {
	}
	
	@Configuration
	@JFishProfile
	@EnableJdbcRepositories(basePackageClasses=TestUserRepository.class)
	@Import(JdbcConfiguration.class)
//	@EnableDbmRepository(value="org.springframework.data.jdbc.dbm", autoRegister=true)
	public static class SpringDataJdbcTestConfig {

		@Bean
		public DatasourceFactoryBean datasourceFactoryBean(){
			DatasourceFactoryBean dfb = new DatasourceFactoryBean();
			dfb.setPrefix("jdbc.");
			dfb.setImplementClass(org.apache.tomcat.jdbc.pool.DataSource.class);
			return dfb;
		}
		
		@Bean
		public PlatformTransactionManager transactionManager(DataSource dataSource) {
			DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
			transactionManager.setDataSource(dataSource);
			return transactionManager;
		  }
		
	}

}
