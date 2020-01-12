package org.onetwo.dbm.ui;

import java.util.Map;

import org.onetwo.common.reflect.ReflectUtils;
import org.onetwo.common.utils.LangUtils;
import org.onetwo.dbm.ui.core.DefaultUIClassMetaManager;
import org.onetwo.dbm.ui.core.DefaultUISelectDataProviderService;
import org.onetwo.dbm.ui.spi.DUIClassMetaManager;
import org.onetwo.dbm.ui.spi.DUISelectDataProviderService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @author weishao zeng
 * <br/>
 */
@Configuration
public class DbmUIConfiguration implements ImportAware {
	
	private String[] packagesToScan;
	
	@Override
	public void setImportMetadata(AnnotationMetadata importMetadata) {
		if(importMetadata==null){
			return ;
		}
		Map<String, Object> annotationAttributes = importMetadata.getAnnotationAttributes(EnableDbmUI.class.getName());
		if(annotationAttributes!=null){
			this.packagesToScan = (String[])annotationAttributes.get("packagesToScan");
		}
		if (LangUtils.isEmpty(packagesToScan)) {
			String importingAnnotationClassName = importMetadata.getClassName();
			String packName = ReflectUtils.loadClass(importingAnnotationClassName).getPackage().getName();
			this.packagesToScan = new String[] {packName};
		}
	}

	@Bean
	public DUIClassMetaManager uiClassMetaManager() {
		DefaultUIClassMetaManager metaManager = new DefaultUIClassMetaManager();
		metaManager.setPackagesToScan(packagesToScan);
		return metaManager;
	}
	
	@Bean
	public DUISelectDataProviderService uiSelectDataProviderService() {
		return new DefaultUISelectDataProviderService();
	}
}
