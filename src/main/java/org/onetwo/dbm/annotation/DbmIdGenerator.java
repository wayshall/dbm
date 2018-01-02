package org.onetwo.dbm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.onetwo.dbm.id.CustomIdGenerator;

/**
 * @author wayshall
 * <br/>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
@SuppressWarnings("rawtypes")
public @interface DbmIdGenerator {
	
	String name();
	/****
	 * try to find in spring context, otherwise new instance
	 * @author wayshall
	 * @return
	 */
	Class<? extends CustomIdGenerator> generatorClass();
	
	String attributes() default "";

}
