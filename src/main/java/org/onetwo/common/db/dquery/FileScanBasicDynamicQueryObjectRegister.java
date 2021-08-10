package org.onetwo.common.db.dquery;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

import org.onetwo.common.db.dquery.DynamicQueryHandlerProxyCreator.DbmRepositoryAttrs;
import org.onetwo.common.db.filequery.SpringBasedSqlFileScanner;
import org.onetwo.common.db.spi.NamedSqlFileManager;
import org.onetwo.common.db.spi.SqlFileScanner;
import org.onetwo.common.log.JFishLoggerFactory;
import org.onetwo.common.propconf.ResourceAdapter;
import org.onetwo.common.reflect.ReflectUtils;
import org.onetwo.common.spring.SpringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ClassUtils;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class FileScanBasicDynamicQueryObjectRegister implements DynamicQueryObjectRegister {
	static private LoadingCache<Method, DynamicMethod> methodCache = CacheBuilder.newBuilder()
			.build(new CacheLoader<Method, DynamicMethod>() {
				@Override
				public DynamicMethod load(Method method) throws Exception {
					return DynamicMethod.newDynamicMethod(method);
				}
			});
	
	protected final Logger logger = JFishLoggerFactory.getLogger(this.getClass());

	private SqlFileScanner sqlFileScanner = new SpringBasedSqlFileScanner(ClassUtils.getDefaultClassLoader());

	//不再根据数据库扫描，数据库类型根据接口的信息读取，扫描时无法确认……
//	private DataBase database;
	private BeanDefinitionRegistry registry;
//	private ApplicationContext applicationContext;
	
	public FileScanBasicDynamicQueryObjectRegister(BeanDefinitionRegistry registry) {
		this.registry = registry;
	}

	public FileScanBasicDynamicQueryObjectRegister(ApplicationContext applicationContext) {
		this.registry = SpringUtils.getBeanDefinitionRegistry(applicationContext);
	}

	/*public void setDatabase(DataBase database) {
		this.database = database;
	}*/


	@Override
	public boolean registerQueryBeans() {
		logger.info("start to register dao bean ....");
		Map<String, ResourceAdapter<?>> sqlfiles = sqlFileScanner.scanMatchSqlFiles(null);
		sqlfiles.entrySet().parallelStream().forEach(f -> {
			String className = f.getKey();
			if(NamedSqlFileManager.GLOBAL_NS_KEY.equalsIgnoreCase(className)){
				return;
			}
			if(registry.containsBeanDefinition(className)){
				return;
			}
			final Class<?> repositoryClass = ReflectUtils.loadClass(className);
			
			Optional<DbmRepositoryAttrs> dbmRepAttrsOpt = DynamicQueryHandlerProxyCreator.findDbmRepositoryAttrs(repositoryClass);
			if (!dbmRepAttrsOpt.isPresent()) {
				logger.info("ignore registered DbmRepository bean: {} ", className);
				return ;
			}
			
			DbmRepositoryAttrs dbmRepAttrs = dbmRepAttrsOpt.get();
			if (DynamicQueryHandlerProxyCreator.isIgnoreRegisterDbmRepository(registry, dbmRepAttrs)) {
				logger.info("ignore registered DbmRepository bean: {} ", className);
				return ;
			}
			
			BeanDefinition beandef = BeanDefinitionBuilder.rootBeanDefinition(DynamicQueryHandlerProxyCreator.class)
								.addConstructorArgValue(repositoryClass)
								.addConstructorArgValue(dbmRepAttrs)
								.addConstructorArgValue(methodCache)
								.addPropertyValue(DynamicQueryHandlerProxyCreator.ATTR_SQL_FILE, f.getValue())
								.setScope(BeanDefinition.SCOPE_SINGLETON)
//								.setRole(BeanDefinition.ROLE_APPLICATION)
								.getBeanDefinition();
			registry.registerBeanDefinition(className, beandef);
			logger.info("registered DbmRepository bean: {} -> {}", className, f.getValue().getFile());
		});
		return true;
		
	}
}
