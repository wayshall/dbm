package org.onetwo.dbm.spring;

import org.onetwo.common.db.dquery.RichModelAndQueryObjectScanTrigger;
import org.onetwo.common.spring.SpringUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

public class DbmRepositoryRegistarOfEnableDbmRepository implements ImportBeanDefinitionRegistrar {
	
	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
		AnnotationAttributes attributes = SpringUtils.getAnnotationAttributes(importingClassMetadata, EnableDbmRepository.class);
		if (attributes == null) {
			throw new IllegalArgumentException(String.format("@%s is not present on importing class '%s' as expected", EnableDbmRepository.class.getSimpleName(), importingClassMetadata.getClassName()));
		}
		
		String useEnableDbmRepositoryClassName = importingClassMetadata.getClassName();
		RichModelAndQueryObjectScanTrigger register = new RichModelAndQueryObjectScanTrigger(registry);
		register.setEnableDbmRepositoryAttributes(attributes);
		register.setUseEnableDbmRepositoryClassName(useEnableDbmRepositoryClassName);
		
		register.scanAndRegisterBeans((ListableBeanFactory)registry);
	}

}
