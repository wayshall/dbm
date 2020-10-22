package org.onetwo.common.db.dquery.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface QueryParseContext {
	
	/****
	 * 指定上下文中特定的变量可以作为sql的命名参数值
	 * @author weishao zeng
	 * @return
	 */
	String[] sqlParameterVars() default {};
	
}
