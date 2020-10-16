package org.onetwo.common.db.dquery.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.onetwo.common.db.spi.SqlTemplateParser;

/**
 * @author wayshall
 * <br/>
 */

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface QuerySqlTemplateParser {

	Class<? extends SqlTemplateParser> value() default SqlTemplateParser.class;

}
