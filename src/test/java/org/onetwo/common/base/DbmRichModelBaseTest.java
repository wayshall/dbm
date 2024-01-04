package org.onetwo.common.base;



import jakarta.annotation.Resource;
import javax.sql.DataSource;

import org.onetwo.common.base.DbmRichModelBaseTest.DbmRichModelBaseTestContextConfig;
import org.onetwo.common.dbm.PackageInfo;
import org.onetwo.common.spring.cache.JFishSimpleCacheManagerImpl;
import org.onetwo.common.spring.config.JFishProfile;
import org.onetwo.common.spring.test.SpringBaseJUnitTestCase;
import org.onetwo.dbm.spring.EnableDbm;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

@ActiveProfiles({ "dev" })
//@ContextConfiguration(value="classpath:/applicationContext-test.xml")
@ContextConfiguration(loader=AnnotationConfigContextLoader.class, classes=DbmRichModelBaseTestContextConfig.class)
//@Rollback(false)
public class DbmRichModelBaseTest extends SpringBaseJUnitTestCase {
	
	@Configuration
	@JFishProfile
	@ImportResource("classpath:conf/applicationContext-test.xml")
	@EnableDbm(packagesToScan="org.onetwo.common.dbm.richmodel")
	@ComponentScan(basePackageClasses=PackageInfo.class)
	public static class DbmRichModelBaseTestContextConfig {

		@Resource
		private DataSource dataSource;
		@Bean
		public CacheManager cacheManager() {
			JFishSimpleCacheManagerImpl cache = new JFishSimpleCacheManagerImpl();
			return cache;
		}
		
	}
}
