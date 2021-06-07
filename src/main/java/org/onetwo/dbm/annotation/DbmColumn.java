package org.onetwo.dbm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.onetwo.dbm.utils.DBUtils;
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DbmColumn {
	
	String name() default "";
	
	/***
	 * @see java.sql.Types
	 */
	int sqlType() default DBUtils.TYPE_UNKNOW;
	
}
