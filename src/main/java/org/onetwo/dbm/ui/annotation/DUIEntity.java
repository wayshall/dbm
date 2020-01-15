package org.onetwo.dbm.ui.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author weishao zeng
 * <br/>
 */

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface DUIEntity {
	
	String name() default "";
	String label();
	
	boolean listPage() default true;
	boolean editPage() default true;
	
	Class<?>[] editableEntities() default {};
}
