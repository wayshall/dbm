package org.onetwo.dbm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DbmResultMapping {
	/***
	 * 根据id属性作为区分一条记录的标志，否则使用对象的hashcode
	 * @author weishao zeng
	 * @return
	 */
	String idField() default "";
	String columnPrefix() default "";
	DbmNestedResult[] value();
}
