package org.onetwo.dbm.ui.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.onetwo.dbm.ui.core.UISelectDataProvider;

/**
 * @author weishao zeng
 * <br/>
 */

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UISelect {
	
	Class<? extends Enum<?>> dataEnumClass() default NoEnums.class;
	
	Class<? extends UISelectDataProvider> dataProvider() default NoProvider.class;
	
	String labelField() default "label";
	String valueField() default "value";
	
//	String remoteUrl() default "/web-admin/dbm/uiselect/dataProvider";
	
	public enum NoEnums {
	}
	public class NoProvider implements UISelectDataProvider {
		@Override
		public Object findDatas(String query) {
			throw new UnsupportedOperationException();
		}
	}
}
