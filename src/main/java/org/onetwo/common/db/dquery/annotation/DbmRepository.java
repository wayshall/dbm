package org.onetwo.common.db.dquery.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.onetwo.common.db.spi.QueryProvideManager;
import org.onetwo.common.db.spi.SqlTemplateParser;

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
	String queryProviderName() default "";
	Class<? extends QueryProvideManager> queryProviderClass() default QueryProvideManager.class;
	String dataSource() default "";
	
	Class<? extends SqlTemplateParser> sqlTemplateParser() default SqlTemplateParser.class;
	
	
}
