package org.onetwo.common.db.dquery.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.persistence.EnumType;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Param {

	public String value();
	
	/***
	 * 如果参数是数组和列表，使用name+index重新生成参数名称
	 * cardNo in ( ${_func.inParams('cardNo', cardNos.size())} )
	 * 
	 * @deprecated 已直接支持List参数
	 * @return
	 */
	@Deprecated
	public boolean renamedUseIndex() default false;
	
	public boolean isLikeQuery() default false;
	
	EnumType enumType() default EnumType.STRING;
	
}
