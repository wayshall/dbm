package org.onetwo.dbm.jdbc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.onetwo.dbm.jdbc.spi.DbmJdbcOperationType;

/*****
 * 标记session操作db相关方法
 * @author way
 *
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DbmJdbcOperationMark {
	
	DbmJdbcOperationType type();
	

}
