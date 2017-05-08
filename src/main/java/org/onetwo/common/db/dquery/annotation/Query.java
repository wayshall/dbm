package org.onetwo.common.db.dquery.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.onetwo.common.db.filequery.FileSqlParserType;

/**
 * @author wayshall
 * <br/>
 */

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Query {
	
	String value();
	
	String countQuery() default "";
	
	FileSqlParserType parser() default FileSqlParserType.TEMPLATE;

}
