package org.onetwo.dbm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DbmNestedResult {
	/***
	 * 指定嵌套映射的属性
	 * @return
	 */
	String property();
	/***
	 * 指定嵌套子类的某个属性作为唯一键，即${property}_id
	 * 用来决定嵌套子类的对象（一行数据）是否是相同，如果此属性的值相同，则无论嵌套对象到其它属性是否相同，均视为同一条数据。
	 * 
	 * @return
	 */
	String id() default "";
	/***
	 * 默认使用"${property}_"，如果property是嵌套的，则"."会转为"_"
	 * @return
	 */
	String columnPrefix() default "";
	NestedType nestedType();
	
	/****
	 * 若嵌套属性为容器类型，是否使用容器的contains方法过滤重复数据
	 * @author weishao zeng
	 * @return
	 */
	boolean filterDuplicate() default true;
	
	public enum NestedType {
		ASSOCIATION,
		COLLECTION,
		MAP
	}
}
