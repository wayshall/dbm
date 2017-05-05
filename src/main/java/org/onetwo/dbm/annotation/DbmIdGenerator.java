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
	Class<? extends CustomIdGenerator> generatorClass();

}
