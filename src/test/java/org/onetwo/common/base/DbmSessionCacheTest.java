package org.onetwo.common.base;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.onetwo.common.base.DbmSessionCacheTest.DbmSessionCacheContextConfig;
import org.onetwo.common.dbm.PackageInfo;
import org.onetwo.common.dbm.model.entity.CompanyEntity;
import org.onetwo.common.dbm.model.service.CompanySerivceImpl;
import org.onetwo.common.spring.cache.JFishSimpleCacheManagerImpl;
import org.onetwo.common.spring.config.JFishProfile;
import org.onetwo.common.spring.test.SpringBaseJUnitTestCase;
import org.onetwo.dbm.core.internal.DebugContextInterceptor;
import org.onetwo.dbm.core.internal.DebugContextInterceptor.DebugContextData;
import org.onetwo.dbm.core.internal.DebugContextInterceptor.InvokeData;
import org.onetwo.dbm.jdbc.spi.DbmJdbcOperationType.DatabaseOperationType;
import org.onetwo.dbm.mapping.DbmConfig;
import org.onetwo.dbm.mapping.DefaultDbmConfig;
import org.onetwo.dbm.spring.EnableDbm;
import org.springframework.beans.factory.annotation.Autowired;
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
@ContextConfiguration(loader=AnnotationConfigContextLoader.class, classes=DbmSessionCacheContextConfig.class)
//@Rollback(false)
public class DbmSessionCacheTest extends SpringBaseJUnitTestCase {
	
	@Autowired
	private CompanySerivceImpl companySerivceImpl;
	
	@Test
	public void testCache(){
		CompanyEntity company = createCompany(1);
		companySerivceImpl.save(company);
		
		CompanyEntity dbCompany = companySerivceImpl.findByNameWithInvoke4Times("测试公司-1");
		assertThat(dbCompany).isNotNull();
		DebugContextData debugs = DebugContextInterceptor.getCurrentDebugContextData().get();
		assertThat(debugs.getSqlAndParamList().size()).isEqualTo(2);
		
		List<Pair<String, Object>> querys = debugs.getSqlAndParamList().stream().filter(p->{
			return p.getKey().toLowerCase().contains("select ");
		})
		.collect(Collectors.toList());
		assertThat(querys.size()).isEqualTo(1);
		
		List<InvokeData> invokes = debugs.getInvokeList().stream().filter(i->{
			return i.getDbOperation().isPresent() && i.getDbOperation().get()==DatabaseOperationType.QUERY;
		})
		.collect(Collectors.toList());
		
		assertThat(invokes.size()).isEqualTo(5);//dbmsession.query,4 + template.query 1
	}
	

	public CompanyEntity createCompany(int index){
		int employeeNumber = 10;
		CompanyEntity company = new CompanyEntity();
		company.setName("测试公司-"+index);
		company.setEmployeeNumber(employeeNumber);
		company.setDescription("一个测试公司-"+index);
		return company;
	}
	
	@Configuration
	@JFishProfile
	@ImportResource("classpath:conf/applicationContext-test.xml")
	@EnableDbm(value="dataSource", enableRichModel=false)
	@ComponentScan(basePackageClasses=PackageInfo.class)
	public static class DbmSessionCacheContextConfig {

		@Resource
		private DataSource dataSource;
		@Bean
		public CacheManager cacheManager() {
			JFishSimpleCacheManagerImpl cache = new JFishSimpleCacheManagerImpl();
			return cache;
		}
		
		@Bean
		public DbmConfig dbmConfig(){
			DefaultDbmConfig config = new DefaultDbmConfig();
			config.setEnableSessionCache(true);
			config.setEnabledDebugContext(true);
			return config;
		}
		
	}
}
