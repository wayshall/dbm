package org.onetwo.dbm.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.onetwo.dbm.mapping.JsonFieldValueConverter;
@Target({FIELD, METHOD})
@Retention(RetentionPolicy.RUNTIME)
@DbmField(converterClass=JsonFieldValueConverter.class)
public @interface DbmJsonField {

}
