package org.onetwo.dbm.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.onetwo.dbm.mapping.converter.SensitiveFieldValueConverter;

/***
 * 敏感字段映射
 * 
 * @author way
 *
 */
@Target({FIELD, METHOD})
@Retention(RetentionPolicy.RUNTIME)
@DbmField(converterClass=SensitiveFieldValueConverter.class)
public @interface DbmSensitiveField {
	
	/***
	 * 脱敏时机
	 * @author weishao zeng
	 * @return
	 */
	SensitiveOns on() default SensitiveOns.SELECT;
	
	/***
	 * 脱敏时需要左边保持明文的字符长度
	 * @author weishao zeng
	 * @return
	 */
	int leftPlainTextSize();
	
	/***
	 * 脱敏时需要右边保持明文的字符长度
	 * @author weishao zeng
	 * @return
	 */
	int rightPlainTextSize() default 0;
	
	/***
	 * 当不想整个字段进行脱敏的时候，此属性表示某个指定的字符索引作为脱敏的结束索引，当这个属性不为空的时候
	 * @author weishao zeng
	 * @return
	 */
	String sensitiveIndexOf() default "";
	
	/****
	 * 替换敏感数据的字符串
	 * @author weishao zeng
	 * @return
	 */
	String replacementString() default "*";
	
	/****
	 * 脱敏时机
	 * @author way
	 *
	 */
	public enum SensitiveOns {
		/****
		 * 存储的时候
		 */
		STORE,
		/***
		 * 获取显示的时候
		 */
		SELECT
	}
}
