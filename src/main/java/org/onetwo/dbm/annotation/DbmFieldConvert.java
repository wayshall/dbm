package org.onetwo.dbm.annotation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.onetwo.dbm.annotation.DbmFieldConvert.DbmFieldConverts;
import org.onetwo.dbm.mapping.DbmFieldValueConverter;

/***
 * 扩展的映射配置注解
 * @author wayshall
 *
 */
@Target({FIELD, METHOD, ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(DbmFieldConverts.class)
public @interface DbmFieldConvert {
	/***
	 * 可自定义值转换器
	 * @author wayshall
	 * @return
	 */
	Class<? extends DbmFieldValueConverter> converterClass();
	
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
	@Inherited
	public @interface DbmFieldConverts {
		
		DbmFieldConvert[] value();
	}
}
