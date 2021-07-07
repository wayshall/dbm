package org.onetwo.common.db.dquery.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/***
 * 指定参数为查询结果类型；
 * 参数必须是class类型参数；
 * 如果参数是数组，则第一个元素为结果集类型，比如容器类型，第二个参数为容器元素类型
 * @author way
 *
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface QueryResultType {
	/****
	 * 通过属性指定数据行映射的类型
	 * @author weishao zeng
	 * @return
	 
	Class<?> rowMappingType() default void.class;
	*/
}
