package org.onetwo.common.db.dquery.repostory;

import java.lang.reflect.Method;
import java.util.Collection;

import org.apache.commons.lang3.ArrayUtils;
import org.onetwo.common.db.dquery.DynamicMethod;
import org.onetwo.common.db.dquery.DynamicQueryObjectRegister;
import org.onetwo.common.db.dquery.annotation.DbmRepository;
import org.onetwo.common.db.spi.QueryProvideManager;
import org.onetwo.common.log.JFishLoggerFactory;
import org.onetwo.common.reflect.ReflectUtils;
import org.onetwo.common.spring.SpringUtils;
import org.onetwo.common.spring.utils.JFishResourcesScanner;
import org.onetwo.dbm.spring.DynamicQueryObjectRegisterConfigration;
import org.slf4j.Logger;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class AnnotationScanBasicDynamicQueryObjectRegister implements DynamicQueryObjectRegister {
	protected final Logger logger = JFishLoggerFactory.getLogger(this.getClass());

	private JFishResourcesScanner resourcesScanner = new JFishResourcesScanner();

	private LoadingCache<Method, DynamicMethod> methodCache = CacheBuilder.newBuilder()
																.build(new CacheLoader<Method, DynamicMethod>() {
																	@Override
																	public DynamicMethod load(Method method) throws Exception {
																		return DynamicMethod.newDynamicMethod(method);
																	}
																});

//	private SqlFileScanner sqlFileScanner = new SpringBasedSqlFileScanner(ClassUtils.getDefaultClassLoader());
	private BeanDefinitionRegistry registry;
//	private ApplicationContext applicationContext;
	private String[] packagesToScan;
	
	private Class<?> defaultQueryProvideManagerClass;
	private boolean registerDefaultQueryProvideManager;
	
	public AnnotationScanBasicDynamicQueryObjectRegister(BeanDefinitionRegistry registry) {
		this.registry = registry;
	}

	public AnnotationScanBasicDynamicQueryObjectRegister(ApplicationContext applicationContext) {
		this.registry = SpringUtils.getBeanDefinitionRegistry(applicationContext);
	}

	public void setDefaultQueryProvideManagerClass(Class<?> defaultQueryProvideManagerClass) {
		this.defaultQueryProvideManagerClass = defaultQueryProvideManagerClass;
	}

	public void setRegisterDefaultQueryProvideManager(boolean registerDefaultQueryProvideManager) {
		this.registerDefaultQueryProvideManager = registerDefaultQueryProvideManager;
	}

	public void setPackagesToScan(String[] packagesToScan) {
		this.packagesToScan = packagesToScan;
	}

	public boolean registerQueryBeans() {
		if(registerDefaultQueryProvideManager && 
				!this.defaultQueryProvideManagerClass.isInterface() && 
				!registry.containsBeanDefinition(defaultQueryProvideManagerClass.getName())){
			BeanDefinition beandef = BeanDefinitionBuilder.rootBeanDefinition(defaultQueryProvideManagerClass)
					.setScope(BeanDefinition.SCOPE_SINGLETON)
					.getBeanDefinition();
			registry.registerBeanDefinition(defaultQueryProvideManagerClass.getName(), beandef);
			logger.info("auto register defaultQueryProvideManagerClass:{}", this.defaultQueryProvideManagerClass);
		}
		if(ArrayUtils.isEmpty(packagesToScan)){
			logger.info("no packages config to scan for DbmRepository ....");
			return false;
		}
		logger.info("start to register dao bean ....");
		Collection<Class<?>> dbmRepositoryClasses = resourcesScanner.scan((metadataReader, res, index)->{
			if( metadataReader.getAnnotationMetadata().hasAnnotation(DbmRepository.class.getName()) ){
				Class<?> cls = ReflectUtils.loadClass(metadataReader.getClassMetadata().getClassName(), false);
				return cls;
			}
			return null;
		}, packagesToScan);
		
		for(Class<?> repositoryClass: dbmRepositoryClasses){
			String className = repositoryClass.getName();
			if(registry.containsBeanDefinition(className)){
				continue;
			}
			BeanDefinition beandef = BeanDefinitionBuilder.rootBeanDefinition(AnnotationBasicJDKDynamicProxyCreator.class)
					.addConstructorArgValue(repositoryClass)
					.addConstructorArgValue(methodCache)
					.setScope(BeanDefinition.SCOPE_SINGLETON)
//					.setRole(BeanDefinition.ROLE_APPLICATION)
					.getBeanDefinition();
			if(defaultQueryProvideManagerClass!=null){
				beandef.setAttribute(DynamicQueryObjectRegisterConfigration.ATTR_DEFAULT_QUERY_PROVIDE_MANAGER_CLASS, defaultQueryProvideManagerClass);
			}
			registry.registerBeanDefinition(className, beandef);
			logger.info("register dao bean: {} ", className);
		}
		boolean scaned = !dbmRepositoryClasses.isEmpty();
		return scaned;
	}
}
