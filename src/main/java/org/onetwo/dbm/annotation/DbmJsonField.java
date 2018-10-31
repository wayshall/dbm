package org.onetwo.dbm.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.onetwo.dbm.mapping.JsonFieldValueConverter;

/***
 * 标注字段为json字段
 * @author way
 *
 */
@Target({FIELD, METHOD})
@Retention(RetentionPolicy.RUNTIME)
@DbmField(converterClass=JsonFieldValueConverter.class)
public @interface DbmJsonField {
	
	/***
	 * 序列化json的时候是否保存类型信息
	 * @author weishao zeng
	 * @return
	 */
	boolean storeTyping() default false;

}
