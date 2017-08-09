package org.onetwo.dbm.spring;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.onetwo.dbm.core.spi.DbmEntityManager;
import org.springframework.context.annotation.Import;

/***
 * 
 * @author wayshall
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import({DbmRepositoryRegistarOfEnableDbmRepository.class})
public @interface EnableDbmRepository {
	
	Class<?> defaultQueryProviderClass() default DbmEntityManager.class;
	
	/***
	 * 是否自动注册 defaultQueryProviderClass
	 * @author wayshall
	 * @return
	 */
	boolean autoRegister() default false;
	
	/****
	 * package to scan
	 * @author wayshall
	 * @return
	 */
	String[] value() default {};
	
	Class<?>[] basePackageClasses() default {};
}
