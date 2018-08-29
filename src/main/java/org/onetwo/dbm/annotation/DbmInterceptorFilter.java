package org.onetwo.dbm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/****
 * 一共有三种不同层面和类型的拦截器：
 * SESSOIN: 在调用session api的时候拦截调用
 * JDBC: 在调用jdbc api的时候拦截调用
 * REPOSITORY: 在调用dbm repository的时候拦截调用
 * 
 * 注意，这三种拦截器是可以同时起作用的
 * 
 * @author wayshall
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DbmInterceptorFilter {
	
	/***
	 * 拦截器类型
	 * @author wayshall
	 * @return
	 */
	InterceptorType[] type();

	public enum InterceptorType {
		SESSION,
		JDBC,
		REPOSITORY
	}
}
