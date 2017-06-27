package org.onetwo.dbm.spring;

import java.util.ArrayList;
import java.util.List;

import org.onetwo.common.spring.context.AbstractImportSelector;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @author wayshall
 * <br/>
 */
public class EnableDbmSelector extends AbstractImportSelector<EnableDbm> {

	@Override
	protected List<String> doSelect(AnnotationMetadata metadata, AnnotationAttributes attributes) {
		List<String> classNames = new ArrayList<String>();
		classNames.add(DbmSpringConfiguration.class.getName());
		boolean enableDbmRepository = attributes.getBoolean("enableDbmRepository");
		if(enableDbmRepository){
			classNames.add(DbmRepositoryRegistarOfEnableDbm.class.getName());
		}
		return classNames;
	}
	
	

}
