package org.onetwo.dbm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * TODO 用于标记非id属性的值生成
 * 占坑，未实现
 * @author weishao zeng
 * <br/>
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DbmGenerated {
	
	GeneratedOn value();
	
	public enum GeneratedOn {
		INSERT,
		UPDATE,
		ALL
	}

}
