package org.onetwo.dbm.annotation;

import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**********
 * 用于配置指定的mapper，默认使用EntryRowMapper
 * 
 * @author wayshall
 *
 */
@Target({TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface DbmRowMapper {
	
	Class<?> value() default Void.class;

}
