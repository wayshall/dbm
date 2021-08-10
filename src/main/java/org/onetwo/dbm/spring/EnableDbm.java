package org.onetwo.dbm.spring;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import({EnableDbmSelector.class})
//@EnableDbmRepository
public @interface EnableDbm {
	
	/****
	 * dataSource bean name
	 * @return
	 */
	String value() default "dataSource";
	/****
	 * package to scan model and repository
	 * @return
	 */
	String[] packagesToScan() default {};
	
	boolean enableRichModel() default true;
	
	/***
	 * 是否启用扫描带有注解 @DbmRepository 的类作为dbm的查询接口，并自动注册到容器
	 * @author wayshall
	 * @return
	 */
	boolean enableDbmRepository() default true;
}
