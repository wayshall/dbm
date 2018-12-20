package org.onetwo.dbm.annotation;

import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**********
 * ENTITY模式
 * 用于配置指定的mapper，默认使用EntryRowMapper
 * EntryRowMapper会使用实体的风格映射，即：
 * 如果有使用@Column注解，则按照注解的映射匹配；
 * 如果没有使用注解，则把属性名称转为下划线匹配；
 * 
 * SMART_PROPERTY模式：
 * 如果不使用此注解，一般都使用DbmBeanPropertyRowMapper映射属性，即：
 * 自动把bean的属性名称转为小写和下划线两种方式去匹配sql返回的列值
 * 
 * MIXTURE 混合模式：
 * 先匹配ENTITY模式，如果没有，则匹配SMART_PROPERTY模式
 * 
 * @author wayshall
 *
 */
@Target({TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface DbmRowMapper {
	
	/****
	 * 如果为Void，则使用mappingMode配置的模式进行映射，
	 * 否则，使用指定的mapper自定义映射，mappingMode属性失效。
	 * 
	 * @author wayshall
	 * @return
	 */
	Class<?> value() default Void.class;
	
	/***
	 * 映射模式
	 * @author wayshall
	 * @return
	 */
	MappingModes mappingMode() default MappingModes.ENTITY;
	
	public enum MappingModes {
		/****
		 * 用于配置指定的mapper，默认使用EntryRowMapper
 * EntryRowMapper会使用实体的风格映射，即：
 * 如果有使用@Column注解，则按照注解的映射匹配；
 * 如果没有使用注解，则把属性名称转为下划线匹配；
		 */
		ENTITY,
		
		/****
		 * 如果不使用此注解，一般都使用DbmBeanPropertyRowMapper映射属性，即：
 * 自动把bean的属性名称转为小写和下划线两种方式去匹配sql返回的列值
		 */
		SMART_PROPERTY,
		
		/***
		 * 先匹配ENTITY模式，如果没有，则匹配SMART_PROPERTY模式
		 */
		MIXTURE
	}

}
