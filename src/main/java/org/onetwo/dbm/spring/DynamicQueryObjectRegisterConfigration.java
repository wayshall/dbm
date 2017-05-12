package org.onetwo.dbm.spring;

import org.onetwo.common.db.dquery.RichModelAndQueryObjectScanTrigger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DynamicQueryObjectRegisterConfigration {
	
	public static final String ATTR_DEFAULT_QUERY_PROVIDE_MANAGER_CLASS = "defaultQueryProvideManagerClass";

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

	@Bean
	static public RichModelAndQueryObjectScanTrigger annotationScanBasicDynamicQueryObjectRegisterTrigger(ApplicationContext applicationContext){
		RichModelAndQueryObjectScanTrigger register = new RichModelAndQueryObjectScanTrigger(applicationContext);
		return register;
	}
	
}
