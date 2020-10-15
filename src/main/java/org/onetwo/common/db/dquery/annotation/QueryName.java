package org.onetwo.common.db.dquery.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.onetwo.common.spring.ftl.TemplateParser;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface QueryName {

	String value();
	
	Class<? extends TemplateParser> templateParser();
	
}
