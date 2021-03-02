package org.onetwo.dbm.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.onetwo.dbm.mapping.converter.JsonFieldValueConverter;

import lombok.AllArgsConstructor;
import lombok.Getter;

/***
 * 标注字段为json字段
 * @author way
 *
 */
@Target({FIELD, METHOD})
@Retention(RetentionPolicy.RUNTIME)
@DbmFieldConvert(converterClass=JsonFieldValueConverter.class)
public @interface DbmJsonField {
	
	/***
	 * 序列化json的时候是否保存类型信息
	 * @author weishao zeng
	 * @return
	 */
	boolean storeTyping() default false;
	
	/***
	 * 保存到数据时，可转换的类型
	 * @author weishao zeng
	 * @return
	 */
	JsonConvertibleTypes convertibleJavaType() default JsonConvertibleTypes.STRING;
	
	/****
	 * 当映射当类型为泛型容器时，使用此属性指定容器当值类型
	 * @author weishao zeng
	 * @return
	 */
	Class<?> valueType() default void.class;
	
	@AllArgsConstructor
	public enum JsonConvertibleTypes {
		STRING(String.class),
		BYTE_ARRAY(byte[].class);
		
		@Getter
		private Class<?> javaType;
	}

}
