package org.onetwo.dbm.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/***
 * 把注解字段的值和指定的字段绑定
 * @author way
 *
 */
@Target({FIELD, METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DbmBindValueToField {
	
	String name();
}
