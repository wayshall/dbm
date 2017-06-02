package org.onetwo.dbm.spring;

import org.onetwo.common.db.dquery.RichModelAndQueryObjectScanTrigger;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

@Configuration
public class DynamicQueryObjectRegisterConfigration implements ImportBeanDefinitionRegistrar {
	
	public static final String ATTR_DEFAULT_QUERY_PROVIDE_MANAGER_CLASS = "defaultQueryProvideManagerClass";
	@Autowired
	private ApplicationContext applicationContext;
	/*private ApplicationContext applicationContext;

	private Class<? extends QueryProvideManager> defaultQueryProvideManagerClass;
	private String[] packagesToScan;
	private boolean registerDefaultQueryProvideManager;*/
	
	/*@Override
	public void setImportMetadata(AnnotationMetadata importMetadata) {
		AnnotationAttributes attrs = AbstractImportSelector.getAnnotationAttributes(importMetadata, EnableDbmRepository.class);
		this.defaultQueryProvideManagerClass = attrs.getClass(ATTR_DEFAULT_QUERY_PROVIDE_MANAGER_CLASS);
		this.packagesToScan = attrs.getStringArray("value");
		this.registerDefaultQueryProvideManager = attrs.getBoolean("registerDefaultQueryProvideManager");
	}*/

	/*@Bean
	static public RichModelAndQueryObjectScanTrigger annotationScanBasicDynamicQueryObjectRegisterTrigger(ApplicationContext applicationContext){
		RichModelAndQueryObjectScanTrigger register = new RichModelAndQueryObjectScanTrigger(applicationContext);
		return register;
	}*/

	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
		RichModelAndQueryObjectScanTrigger register = new RichModelAndQueryObjectScanTrigger(registry);
		register.scanAndRegisterBeans((ListableBeanFactory)registry);
	}
	
}
