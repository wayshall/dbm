package org.onetwo.dbm.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.onetwo.dbm.mapping.converter.EncryptFieldValueConverter;

/***
 * 标注字段为加密自动
 * 存储的时候自动加密，获取的时候自动解密
 * @author way
 *
 */
@Target({FIELD, METHOD})
@Retention(RetentionPolicy.RUNTIME)
@DbmFieldConvert(converterClass=EncryptFieldValueConverter.class)
public @interface DbmEncryptField {
}
