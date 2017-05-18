package org.onetwo.dbm.spring;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.onetwo.dbm.core.spi.DbmEntityManager;
import org.springframework.context.annotation.Import;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import({DynamicQueryObjectRegisterConfigration.class})
public @interface EnableDbmRepository {
	
	Class<?> defaultQueryProviderClass() default DbmEntityManager.class;
	
	boolean autoRegister() default false;
	
	/****
	 * package to scan
	 * @author wayshall
	 * @return
	 */
	String[] value() default {};
	
	Class<?>[] basePackageClasses() default {};
}
