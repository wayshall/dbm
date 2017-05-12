package org.onetwo.dbm.spring;

import org.onetwo.common.db.dquery.RichModelAndQueryObjectScanTrigger;
import org.onetwo.common.db.spi.QueryProvideManager;
import org.onetwo.common.spring.context.AbstractImportSelector;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

@Configuration
public class DynamicQueryObjectRegisterConfigration implements ApplicationContextAware, ImportAware {
	
	public static final String ATTR_DEFAULT_QUERY_PROVIDE_MANAGER_CLASS = "defaultQueryProvideManagerClass";

	private ApplicationContext applicationContext;

	private Class<? extends QueryProvideManager> defaultQueryProvideManagerClass;
	
	@Override
	public void setImportMetadata(AnnotationMetadata importMetadata) {
		AnnotationAttributes attrs = AbstractImportSelector.getAnnotationAttributes(importMetadata, EnableDbmRepository.class);
		this.defaultQueryProvideManagerClass = attrs.getClass(ATTR_DEFAULT_QUERY_PROVIDE_MANAGER_CLASS);
	}

	@Bean
	public RichModelAndQueryObjectScanTrigger annotationScanBasicDynamicQueryObjectRegisterTrigger(){
		RichModelAndQueryObjectScanTrigger register = new RichModelAndQueryObjectScanTrigger(applicationContext);
		register.setDefaultQueryProvideManagerClass(defaultQueryProvideManagerClass);
		return register;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}
	
}
