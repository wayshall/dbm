package org.onetwo.common.db.dquery.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/***
 * 优先查找provideManager，如果没有找到，则查找dataSource
 * @author wayshall
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DbmRepository {
	
	/*****
	 * QueryProvideManager beanName
	 * @return
	 */
	String provideManager() default "";
	String dataSource() default "";
	
	
}
