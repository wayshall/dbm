package org.onetwo.dbm.annotation;

import static jakarta.persistence.GenerationType.AUTO;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.persistence.GenerationType;

/**
 * 对应 jpa @GeneratedValue 
 * @author wayshall
 * <br/>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
public @interface DbmGeneratedValue {

    GenerationType strategy() default AUTO;

    String generator() default "";
	
}
