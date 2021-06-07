package org.onetwo.common.db.dquery.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.onetwo.common.file.FileUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

/***
 * 
 * @author way
 * @see ResourceDatabasePopulator
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SqlScript {
	
	boolean isContinueOnError() default false;
	String separator() default ";";
	String sqlScriptEncoding() default FileUtils.UTF8;

}
